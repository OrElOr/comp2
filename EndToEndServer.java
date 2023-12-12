package endtoend;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class EndToEndServer {

    private static List<Socket> clients = new ArrayList<>();
    private static List<String> wordList = new ArrayList<>();
    private static Stack<String> stack = new Stack<>();
    private ServerSocket serverSocket;
    public EndToEndServer() {
        try {
            readText();
            stack.push(wordList.get(0));

            serverSocket = new ServerSocket(12345);
            System.out.println("서버 시작. 클라이언트 연결 대기 중...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // 클라이언트 처리를 위한 스레드 생성
                clients.add(clientSocket);
                System.out.println("클라이언트 연결됨: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //사전 초기화 메서드
    public void readText() throws IOException{
        try(BufferedReader in = new BufferedReader(new FileReader("C:/Users/chj10/OneDrive/문서/카카오톡 받은 파일/dictionary.txt"))){
            String str;
            while((str=in.readLine())!=null){
                wordList.add(str);
            }
            Collections.shuffle(wordList);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new EndToEndServer();
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader reader;

        public ClientHandler(Socket clientSocket) {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.clientSocket = clientSocket;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                String tryword;
                while ((message = reader.readLine()) != null) {

                    if (message.startsWith("Game:")) { // Game에서 온 문자인 경우
                        tryword = message.replace("Game:", "");//message에서 Game: 를 없애줌
                        logic(tryword);
                    } else if(message.equals("GetTargetWord")) {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        writer.write(stack.peek());
                        writer.newLine();
                        writer.flush();
                    } else { // Chat에서 온 문자인 경우
                        chatbroadcast(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 종료");

                if (!clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch(IOException ex) {
                        System.err.println("소켓 닫기 실패");
                    }
                }
            }
            System.out.println("run함수 끝");
        }

        public void logic(String word) {
            String backword = stack.peek();
            System.out.println(backword);
            if (backword.charAt(backword.length() - 1)==word.charAt(0) && wordList.contains(word) && !stack.contains(word)){
                try {
                    gamewordbroadcast(word);
                    stack.push(word);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //backword(기존단어), word(사용자가 입력한 단어)
            //끝말잇기 로직구현
            //1. backword(기존단어)의 마지막 글자와 word(사용자가 입력한 단어)의 첫글자가 같은지
            //2. word가 wordList에 포함되어 있는지

            //만족한다면 word를 매개변수로 gamewordbroadcast함수호출하고, stack에 word를 추가.
            //TODO: pass(한방단어 포기)같은 기능은 구현 안됨. 생각보다 wordlist의 단어가 부족하게 느껴짐. 두음법칙은 구현 안됨.
//            try {
//                gamewordbroadcast(word);
//            }catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        //inputField에서 보낸 문자를 다른 클라이언트들에게 전송하는 메서드
        private void chatbroadcast(String message) throws IOException{
            clients.removeIf(Socket::isClosed);
            for(Socket client : clients) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        }

        //GameField에서 보낸 문자를 다른 클라이언트들에게 전송하는 메서드
        //이떄는 다른 클라이언트들에게 전송할 때 "Game:"을 더해서 전송
        private void gamewordbroadcast(String message) throws IOException{
            clients.removeIf(Socket::isClosed);
            for(Socket client : clients) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                writer.write("Game:"+ message);
                writer.newLine();
                writer.flush();
            }
        }
    }
}
