package WordChain;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class WordChainServer {
    private static Map<Socket, ClientInfo> clients = new HashMap<>();

    //    private static List<Socket> clients = new ArrayList<>();//클라이언트 소켓 리스트
    private static List<String> wordList = new ArrayList<>();//단어사전 리스트
    private static Stack<String> stack = new Stack<>();//사용한 단어 스택
    private ServerSocket serverSocket;
    public WordChainServer() {
        try {
            readText();
            //초기 단어 설정
            stack.push(wordList.get(0));

            serverSocket = new ServerSocket(12345);
            System.out.println("서버 시작. 클라이언트 연결 대기 중...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream objectReader = new ObjectInputStream(clientSocket.getInputStream());

                ClientInfo clientInfo = null;

                System.out.println("clientInfo 받아오는중");
                while (clientInfo == null) {
                    try {
                        clientInfo = (ClientInfo) objectReader.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("받아오기 성공");
                //제대로 됐는지 확인
                System.out.println(clientInfo);

                clients.put(clientSocket, clientInfo);
                System.out.println("클라이언트 연결됨: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //단어 사전 초기화 메서드
    public void readText() throws IOException{
        try(BufferedReader in = new BufferedReader(new FileReader("src/WordChain/dictionary.txt"))){
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
        new WordChainServer();
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

                    if (message.startsWith("Game:")) { // gameInputField에서 온 문자인 경우
                        tryword = message.replace("Game:", "");//message에서 Game: 를 없애줌
                        logic(tryword);
                    } else if (message.equals("GetTargetWord:")) {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        writer.write(stack.peek());
                        writer.newLine();
                        writer.flush();
                    } else { // inputField에서 온 문자인 경우
                        chatbroadcast(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 종료");

                if (!clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        System.err.println("소켓 닫기 실패");
                    }
                }
            }
        }

        // 끝말잇기 조건을 구현한 메서드
        public void logic(String word) {
            String backword = stack.peek();
            //backword(기존단어), word(사용자가 입력한 단어)
            //끝말잇기 로직구현
            //만족한다면 word를 매개변수로 gamewordbroadcast함수호출
            if (backword.charAt(backword.length() - 1) != word.charAt(0)) {
                try {
                    sendMessageToClient(clientSocket, "틀렸습니다.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(!wordList.contains(word)){
                try {
                    sendMessageToClient(clientSocket, "사용할 수 없는 단어입니다.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(stack.contains(word)){
                try {
                    sendMessageToClient(clientSocket, "이미 사용된 단어입니다.");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
                stack.push(word);
                waitClient(clientSocket);
                try {
                    clients.get(clientSocket).upScore(word.length());
                    gamewordbroadcast(word);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void waitClient(Socket clientSocket) {
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                writer.write("Wait:");
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void sendMessageToClient(Socket clientSocket, String message) throws IOException {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            writer.write(message);
            writer.newLine();
            writer.flush();
        }

        // 각 클라이언트들에게 채팅 전송
        private void chatbroadcast(String message) throws IOException{
            //clients.removeIf(Socket::isClosed);
            for(Socket client : clients.keySet()) {
                if (client.isClosed()) {
                    clients.remove(client);
                    continue;
                }
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        }

        // 각 클라이언트들에게 바뀐 targetword 전송
        private void gamewordbroadcast(String message) throws IOException{
            //clients.removeIf(Socket::isClosed);
            for(Socket client : clients.keySet()) {
                if (client.isClosed()) {
                    clients.remove(client);
                    continue;
                }
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                writer.write("Game:"+ message);
                writer.newLine();
                writer.flush();

                writer.write(clients.get(clientSocket).getUsername()+"님이 정답을 맞췄습니다.");
                writer.write(clients.get(clientSocket).getUsername()+"님의 점수는 "+clients.get(clientSocket).getScore()
                        +"(+"+stack.peek().length()+")"+" 입니다.");
                writer.newLine();
                writer.flush();

            }
        }

//        private void chatbroadcast(String message) throws IOException{
//            clients.removeIf(Socket::isClosed);
//            for(Socket client : clients) {
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
//                writer.write(message);
//                writer.newLine();
//                writer.flush();
//            }
//        }
//
//        // 각 클라이언트들에게 바뀐 targetword 전송
//        private void gamewordbroadcast(String message) throws IOException{
//            clients.removeIf(Socket::isClosed);
//            for(Socket client : clients) {
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
//                writer.write("Game:"+ message);
//                writer.newLine();
//                writer.flush();
//            }
    }

}
