package io.github.makaseloli.creativemusicinsurvival.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.makaseloli.creativemusicinsurvival.Constants;
import io.github.makaseloli.creativemusicinsurvival.music.MusicChoice;
import io.github.makaseloli.creativemusicinsurvival.music.DimensionMusicSource;
import io.github.makaseloli.creativemusicinsurvival.music.MusicDimension;
import io.github.makaseloli.creativemusicinsurvival.music.MusicGenre;
import io.github.makaseloli.creativemusicinsurvival.music.MusicSelectionConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class WebUiServer {
    private static final int BASE_PORT = 56001;
    private static HttpServer server;
    private static Supplier<String> languageSupplier = () -> "en_us";

    private WebUiServer() {}

    public static synchronized void start(Supplier<String> currentLanguageSupplier) {
        if (server != null) {
            return;
        }
        languageSupplier = currentLanguageSupplier;

        for (int port = BASE_PORT; port < BASE_PORT + 100; port++) {
            try {
                HttpServer candidate = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
                candidate.createContext("/", WebUiServer::handle);
                candidate.setExecutor(Executors.newSingleThreadExecutor(r -> {
                    Thread thread = new Thread(r, Constants.MODID + "-webui");
                    thread.setDaemon(true);
                    return thread;
                }));
                candidate.start();
                server = candidate;
                Constants.LOGGER.info("Adaptive Music Control Web UI is listening on http://127.0.0.1:{}", port);
                return;
            } catch (IOException e) {
                // Try the next port.
            }
        }
        Constants.LOGGER.warn("Failed to bind Adaptive Music Control Web UI from port {} upward", BASE_PORT);
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if ("GET".equals(method) && "/".equals(path)) {
            writeHtml(exchange, page(MusicGenre.MENU));
            return;
        }
        if ("GET".equals(method) && "/now-playing".equals(path) && NowPlayingState.isSupported()) {
            writeJson(exchange, nowPlayingJson());
            return;
        }
        if ("POST".equals(method) && "/next".equals(path) && NowPlayingState.isSupported()) {
            NowPlayingState.requestSkip();
            writeJson(exchange, "{\"accepted\":true}");
            return;
        }
        if ("GET".equals(method) && path.startsWith("/tab/")) {
            String[] parts = path.substring("/tab/".length()).split("/");
            MusicGenre target = MusicGenre.byId(parts[0]);
            String section = parts.length > 1 ? validSection(parts[1]) : "general";
            writeHtml(exchange, target == null || section == null ? notFound() : tab(target, section));
            return;
        }
        if ("POST".equals(method) && "/toggle".equals(path)) {
            Map<String, String> form = readForm(exchange);
            MusicGenre target = MusicGenre.byId(form.get("target"));
            MusicChoice choice = MusicChoice.byId(form.get("choice"));
            MusicDimension dimension = MusicDimension.byId(form.get("dimension"));
            DimensionMusicSource source = DimensionMusicSource.byId(form.get("source"));
            String section = validSection(form.get("section"));
            boolean selected = "on".equals(form.get("selected"));
            if (target != null && choice != null) {
                MusicSelectionConfig.setSelected(target, choice, selected);
                writeHtml(exchange, tab(target, section == null ? "general" : section));
                return;
            }
            if (target != null && dimension != null && source != null) {
                MusicSelectionConfig.setSelected(target, dimension, source, selected);
                writeHtml(exchange, tab(target, dimension.id()));
                return;
            }
        }
        if ("POST".equals(method) && "/reset".equals(path)) {
            Map<String, String> form = readForm(exchange);
            MusicGenre target = MusicGenre.byId(form.get("target"));
            String section = validSection(form.get("section"));
            if (target != null) {
                MusicSelectionConfig.reset(target, MusicDimension.byId(section));
                writeHtml(exchange, tab(target, section == null ? "general" : section));
                return;
            }
        }
        writeHtml(exchange, notFound(), 404);
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            values.put(decode(key), decode(value));
        }
        return values;
    }

    private static String page(MusicGenre selected) {
        Translations.Translator t = Translations.load(languageSupplier.get());
        return "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + esc(t.translate("creativemusicinsurvival.web.title")) + "</title>"
                + "<script>" + htmxLite() + "</script>"
                + "<style>" + css() + "</style></head><body>"
                + "<main><h1>" + esc(t.translate("creativemusicinsurvival.web.title")) + "</h1>"
                + "<section id=\"content\">" + tab(selected, "general") + "</section>"
                + nowPlaying(t)
                + "</main></body></html>";
    }

    private static String nowPlaying(Translations.Translator t) {
        if (!NowPlayingState.isSupported()) {
            return "";
        }
        return "<section class=\"now-playing-wrap\"><div id=\"now-playing\" class=\"now-playing\">"
                + "<span class=\"music-note\">&#9835;</span><span id=\"track-name\">"
                + esc(t.translate("creativemusicinsurvival.web.not_playing")) + "</span>"
                + "<button id=\"next-track\" class=\"next-track\" title=\""
                + esc(t.translate("creativemusicinsurvival.web.next")) + "\" aria-label=\""
                + esc(t.translate("creativemusicinsurvival.web.next")) + "\">&#9197;</button></div></section>";
    }

    private static String nowPlayingJson() {
        NowPlayingState.Track track = NowPlayingState.current();
        if (track == null) {
            return "{\"playing\":false}";
        }
        return "{\"playing\":true,\"artist\":\"" + json(track.artist()) + "\",\"title\":\""
                + json(track.title()) + "\",\"display\":\"" + json(track.display()) + "\"}";
    }

    private static String tab(MusicGenre selected, String section) {
        Translations.Translator t = Translations.load(languageSupplier.get());
        StringBuilder html = new StringBuilder();
        html.append(nav(selected, t));
        html.append("<div class=\"panel\">");
        html.append("<div class=\"panel-heading\"><h2>").append(esc(t.translate(selected.translationKey()))).append("</h2>")
                .append("<button class=\"reset\" hx-post=\"/reset\" hx-target=\"#content\" form=\"f-reset-")
                .append(selected.id()).append("\">")
                .append(esc(t.translate("creativemusicinsurvival.web.reset"))).append("</button></div>")
                .append("<form id=\"f-reset-").append(selected.id()).append("\" class=\"hidden\">")
                .append("<input name=\"target\" value=\"").append(esc(selected.id())).append("\">")
                .append("<input name=\"section\" value=\"").append(esc(section)).append("\"></form>");
        html.append("<p class=\"muted\">").append(esc(t.translate("creativemusicinsurvival.web.description"))).append("</p>");
        html.append(sectionNav(selected, section, t));
        html.append("<div class=\"choices\">");
        MusicDimension dimension = MusicDimension.byId(section);
        if (dimension == null) {
            for (MusicChoice choice : MusicChoice.values()) {
                html.append("<label class=\"choice\">")
                        .append(choiceCheckbox(selected, section, choice))
                        .append("<span>").append(esc(t.translate(choice.translationKey()))).append("</span>")
                        .append("<code>").append(esc(choice.soundId())).append("</code>")
                        .append("</label>");
            }
        } else {
            for (DimensionMusicSource source : DimensionMusicSource.values()) {
                html.append("<label class=\"choice\">")
                        .append(sourceCheckbox(selected, dimension, source))
                        .append("<span>").append(esc(t.translate(source.translationKey()))).append("</span>")
                        .append("<code>").append(esc(source.soundId())).append("</code>")
                        .append("</label>");
            }
        }
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    private static String nav(MusicGenre selected, Translations.Translator t) {
        StringBuilder html = new StringBuilder("<nav class=\"tabs\">");
        for (MusicGenre target : MusicGenre.values()) {
            html.append("<button class=\"tab");
            if (target == selected) {
                html.append(" active");
            }
            html.append("\" hx-get=\"/tab/").append(target.id()).append("/general\" hx-target=\"#content\">")
                    .append(esc(t.translate(target.translationKey())))
                    .append("</button>");
        }
        html.append("</nav>");
        return html.toString();
    }

    private static String sectionNav(MusicGenre target, String selected, Translations.Translator t) {
        StringBuilder html = new StringBuilder("<nav class=\"subtabs\">");
        for (String section : new String[] {"general", "overworld", "nether", "end"}) {
            String key = section.equals("general")
                    ? "creativemusicinsurvival.web.general"
                    : "creativemusicinsurvival.dimension." + section;
            html.append("<button class=\"subtab");
            if (section.equals(selected)) {
                html.append(" active");
            }
            html.append("\" hx-get=\"/tab/").append(target.id()).append('/').append(section)
                    .append("\" hx-target=\"#content\">").append(esc(t.translate(key))).append("</button>");
        }
        return html.append("</nav>").toString();
    }

    private static String choiceCheckbox(MusicGenre target, String section, MusicChoice choice) {
        return checkbox(target.id(), section, "choice", choice.id(), MusicSelectionConfig.isSelected(target, choice));
    }

    private static String sourceCheckbox(MusicGenre target, MusicDimension dimension, DimensionMusicSource source) {
        return checkbox(target.id(), dimension.id(), "source", source.id(), MusicSelectionConfig.isSelected(target, dimension, source));
    }

    private static String validSection(String section) {
        return switch (section == null ? "" : section) {
            case "general", "overworld", "nether", "end" -> section;
            default -> null;
        };
    }

    private static String checkbox(String target, String section, String field, String value, boolean selected) {
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"checkbox\" name=\"selected\" ");
        if (selected) {
            html.append("checked ");
        }
        html.append("hx-post=\"/toggle\" hx-target=\"#content\" hx-include=\"closest form\" form=\"f-")
                .append(field).append('-').append(target).append('-').append(value).append("\">");
        html.append("<form id=\"f-").append(field).append('-').append(target).append('-').append(value).append("\" class=\"hidden\">")
                .append("<input name=\"target\" value=\"").append(esc(target)).append("\">")
                .append("<input name=\"section\" value=\"").append(esc(section)).append("\">");
        if (!section.equals("general")) html.append("<input name=\"dimension\" value=\"").append(esc(section)).append("\">");
        html.append("<input name=\"").append(field).append("\" value=\"").append(esc(value)).append("\">");
        html.append("</form>");
        return html.toString();
    }

    private static String notFound() {
        return "<p>Not found</p>";
    }

    private static void writeHtml(HttpExchange exchange, String html) throws IOException {
        writeHtml(exchange, html, 200);
    }

    private static void writeHtml(HttpExchange exchange, String html, int status) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private static void writeJson(HttpExchange exchange, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String esc(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String css() {
        return """
                body{font:15px/1.4 sans-serif}
                main{max-width:960px;margin:0 auto;padding:16px}
                .tabs{display:flex;gap:4px;margin-bottom:12px;border-bottom:1px solid #bbb}
                .tab{padding:8px 12px;border:0;background:none}
                .tab.active{font-weight:700;border-bottom:2px solid currentColor}
                .subtabs{display:flex;gap:4px;margin:14px 0 10px;border-bottom:1px solid #bbb}
                .subtab{padding:7px 10px;border:0;background:none}
                .subtab.active{font-weight:700;border-bottom:2px solid currentColor}
                .panel-heading{display:flex;align-items:center;justify-content:space-between;gap:12px}
                .panel-heading h2{margin:0}
                .reset{padding:7px 11px}
                .choices{margin-top:12px}
                .choice{display:grid;grid-template-columns:24px minmax(140px,1fr) minmax(180px,auto);gap:8px;align-items:center;margin:6px 0}
                input[type=checkbox]{width:18px;height:18px}
                code{overflow-wrap:anywhere}
                .hidden{display:none}
                .now-playing-wrap{position:fixed;left:50%;bottom:20px;z-index:20;transform:translateX(-50%);display:flex;justify-content:center;width:min(760px,calc(100vw - 30px));pointer-events:none}
                .now-playing{display:grid;grid-template-columns:32px minmax(0,auto) 38px;align-items:center;gap:12px;width:100%;padding:10px 12px;background:#202020;color:#ddd;border:5px solid #555;box-shadow:inset 0 0 0 2px #111;font:20px/1.2 monospace;pointer-events:auto}
                .music-note{color:#888;font-size:28px;text-align:center}
                .now-playing.playing .music-note{animation:music-note-rainbow 2.4s linear infinite}
                @keyframes music-note-rainbow{0%{color:#f25b5b}16%{color:#e5d44f}33%{color:#62d36f}50%{color:#53d5d9}66%{color:#6688ed}83%{color:#c45be8}100%{color:#f25b5b}}
                #track-name{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;text-align:center}
                .next-track{width:36px;height:32px;padding:0;border:2px solid #777;background:#303030;color:#ddd;font-size:18px;cursor:pointer}
                .next-track:hover{background:#444;color:#fff}
                .next-track:disabled{opacity:.5;cursor:default}
                @media(max-width:700px){.choice{grid-template-columns:24px 1fr}.choice code{grid-column:2}}
                """;
    }

    private static String htmxLite() {
        return """
                window.htmx={version:'creativemusicinsurvival-lite'};
                document.addEventListener('click',function(e){var el=e.target.closest('[hx-get]');if(el){fetch(el.getAttribute('hx-get')).then(r=>r.text()).then(h=>{document.querySelector(el.getAttribute('hx-target')).innerHTML=h;});return;}el=e.target.closest('button[hx-post]');if(!el)return;e.preventDefault();var form=document.getElementById((el.getAttribute('form')||'').trim());var data=new URLSearchParams(new FormData(form));fetch(el.getAttribute('hx-post'),{method:'POST',body:data}).then(r=>r.text()).then(h=>{document.querySelector(el.getAttribute('hx-target')).innerHTML=h;});});
                document.addEventListener('change',function(e){var el=e.target.closest('[hx-post]');if(!el)return;var form=document.getElementById((el.getAttribute('form')||'').trim());var data=new URLSearchParams(new FormData(form));if(el.checked)data.set('selected','on');fetch(el.getAttribute('hx-post'),{method:'POST',body:data}).then(r=>r.text()).then(h=>{document.querySelector(el.getAttribute('hx-target')).innerHTML=h;});});
                function refreshNowPlaying(){var name=document.getElementById('track-name');if(!name)return;fetch('/now-playing',{cache:'no-store'}).then(r=>r.json()).then(d=>{name.textContent=d.playing?d.display:name.dataset.empty;name.closest('.now-playing').classList.toggle('playing',d.playing);}).catch(()=>{});}
                document.addEventListener('DOMContentLoaded',function(){var name=document.getElementById('track-name');if(!name)return;name.dataset.empty=name.textContent;var next=document.getElementById('next-track');next.addEventListener('click',function(){next.disabled=true;fetch('/next',{method:'POST'}).finally(()=>{setTimeout(function(){next.disabled=false;refreshNowPlaying();},300);});});refreshNowPlaying();setInterval(refreshNowPlaying,1000);});
                """;
    }
}
