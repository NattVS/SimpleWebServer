import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

final class SolicitudHttp implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    public SolicitudHttp(Socket socket) throws Exception {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            procesarSolicitud();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void procesarSolicitud() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        String lineaDeLaSolicitudHttp = in.readLine();
        System.out.println("Solicitud: " + lineaDeLaSolicitudHttp);
        
        StringTokenizer partesSolicitud = new StringTokenizer(lineaDeLaSolicitudHttp);
        String metodo = partesSolicitud.nextToken();
        String archivo = partesSolicitud.nextToken();

        System.out.println("Método: " + metodo);
        System.out.println("Archivo: " + archivo);

        if (archivo.equals("/")) {
            archivo = "/landingPage.html";
        }

        File file = new File("src\\main\\resources" + archivo);

        InputStream inputStream = null;
        if (file.exists() && file.isFile()) {
            inputStream = new FileInputStream(file);
        } else {
            //Si el archivo no existe, 404
            file = new File("src\\main\\resources\\404.html");
            if (file.exists() && file.isFile()) {
                inputStream = new FileInputStream(file);
            } else {
                //404 manual x si acaso
                sendString("HTTP/1.1 404 Not Found" + CRLF, out);
                sendString("Content-Type: text/html" + CRLF, out);
                sendString(CRLF, out);
                sendString("<h1>404 Not Found</h1>", out);
                out.flush();
                out.close();
                in.close();
                socket.close();
                return;
            }
        }

        //Tipo de contenido y tamaño del archivo
        String contentType = obtainContentType(file.getName());
        long fileSize = file.length();

        //Envía cabecera HTTP
        sendString("HTTP/1.1 200 OK" + CRLF, out);
        sendString("Content-Type: " + contentType + CRLF, out);
        sendString("Content-Length: " + fileSize + CRLF, out);
        sendString(CRLF, out);

        //Envía el contenido dl archivo
        sendBytes(inputStream, out);
        inputStream.close();

        out.flush();
        out.close();
        in.close();
        socket.close();
    }

    private static void sendString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(InputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String obtainContentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".html")) {
            return "text/html";}
        if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg")) {
            return "image/jpeg";}
        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";}
        return "application/octet-stream"; //Defectp
    }
}
