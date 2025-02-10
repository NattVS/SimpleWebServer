import java.net.*; 

class ServidorWebSimple {
   public static void main(String argv[]) throws Exception {

      // El socket socketdeEscucha atenderá servicios en el puerto 6789 
      ServerSocket socketdeEscucha = new ServerSocket(6789); 

      while (true) {
         
         //El método accept() de socketdeEscucha creará un nuevo objeto: socketdeConexion
         Socket socketdeConexion = socketdeEscucha.accept(); 

         SolicitudHttp solicitud = new SolicitudHttp(socketdeConexion);

         Thread hilo = new Thread(solicitud);
         hilo.start();

      }
   }
}