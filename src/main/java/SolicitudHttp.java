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
        System.out.println("Solictud: " + lineaDeLaSolicitudHttp);
        StringTokenizer partesSolicitud = new StringTokenizer(lineaDeLaSolicitudHttp);
        String metodo = partesSolicitud.nextToken();
        String archivo = partesSolicitud.nextToken();
        
        System.out.println("Método: " + metodo);
        System.out.println("Archivo: " + archivo);
    
        File file = new File("src\\main\\resources" + archivo);

        if (file.isDirectory()) { 
            file = new File("src\\main\\resources/index.html"); // Usa un archivo por defecto
        }

        InputStream inputStream = null;
        if (file.exists() && file.isFile()) { 
            inputStream = new FileInputStream(file);
        }
    
        if (inputStream != null) {
            //200 OK si no es null
            enviarString("HTTP/1.1 200 OK" + CRLF, out);
            enviarString("Content-Type: text/html" + CRLF, out);
            enviarString(CRLF, out);
            enviarBytes(inputStream, out);
            inputStream.close();
        } else {
            //404 si es null
            enviarString("HTTP/1.1 404 Not Found" + CRLF, out);
            enviarString("Content-Type: text/html" + CRLF, out);
            enviarString(CRLF, out);
            enviarString("<h1>404 Not Found</h1>", out);
        }

        out.flush();
        out.close();
        in.close();
        socket.close();
    }
    

    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        // Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copia el archivo solicitado hacia el output stream del socket.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

}
