package ai_chat;


/*import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;*/
public class MarkdownRenderer {
/*
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderer() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    public String parse(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    public String createHtml(String markdown) {
        String html = parse(markdown);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        html, body {
                            margin: 0;
                            padding: 0;
                            background: transparent;
                            font-family: Arial, sans-serif;
                            font-size: 14px;
                            color: #202020;
                        }

                        body {
                            padding: 2px;
                            line-height: 1.5;
                            overflow-x: hidden;
                        }

                        p {
                            margin: 0 0 10px 0;
                        }

                        p:last-child {
                            margin-bottom: 0;
                        }

                        h1, h2, h3, h4, h5, h6 {
                            margin-top: 16px;
                            margin-bottom: 8px;
                        }

                        h1 { font-size: 24px; }
                        h2 { font-size: 20px; }
                        h3 { font-size: 17px; }

                        ul, ol {
                            margin-top: 6px;
                            margin-bottom: 10px;
                            padding-left: 25px;
                        }

                        li {
                            margin-bottom: 4px;
                        }

                        code {
                            background: #eeeeee;
                            border-radius: 4px;
                            padding: 2px 5px;
                            font-family: monospace;
                        }

                        pre {
                            background: #eeeeee;
                            border-radius: 6px;
                            padding: 12px;
                            overflow-x: auto;
                            margin: 10px 0;
                        }

                        pre code {
                            background: transparent;
                            padding: 0;
                        }

                        blockquote {
                            margin: 10px 0;
                            padding-left: 12px;
                            border-left: 3px solid #d0d0d0;
                            color: #666666;
                        }

                        table {
                            border-collapse: collapse;
                            margin: 10px 0;
                        }

                        th, td {
                            border: 1px solid #dddddd;
                            padding: 6px 10px;
                            text-align: left;
                        }

                        th {
                            background: #f1f1f1;
                        }

                        hr {
                            border: none;
                            border-top: 1px solid #dddddd;
                            margin: 15px 0;
                        }

                        a {
                            color: #444444;
                        }
                    </style>
                </head>
                <body>
                    %s
                </body>
                </html>
                """.formatted(html);
    }*/
}
