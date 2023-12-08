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
                System.out.println("clientHandler 스레드 시작");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //사전 초기화 메서드
    public void readText() throws IOException{
        try(BufferedReader in = new BufferedReader(new FileReader("src/endtoend/dictionary.txt"))){
            String str;
            while((str=in.readLine())!=null){
                wordList.add(str);
            }
            Collections.shuffle(wordList);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void initTargetword() {

    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader reader;

        public ClientHandler(Socket clientSocket) {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("reader 선언");
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
                System.out.println("run함수 시작");
                System.out.println(0);
                while ((message = reader.readLine()) != null) {
                    System.out.println(1);

                    if (message.startsWith("Game:")) { // Game에서 온 문자인 경우
                        System.out.println("게임에서 온 문자");
                        tryword = message.replace("Game:", "");//message에서 Game: 를 없애줌
                        logic(tryword);

                    } else { // Chat에서 온 문자인 경우
                        System.out.println("채팅에서 온 문자");
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
            //기존 단어 얻어와야함
            //word는 받아온 단어
            //끝말잇기 로직구현
            //word를 매개변수로 gamewordbroadcast함수호출
            System.out.println("로직 함수 시작");
            try {
                gamewordbroadcast(word);
            }catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("로직 함수 종료");
        }

        //inputField에서 보낸 문자를 다른 클라이언트들에게 전송하는 메서드


        private void chatbroadcast(String message) throws IOException{
            clients.removeIf(Socket::isClosed);
            for(Socket client : clients) {
                System.out.println(4);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                writer.write(message);
                writer.newLine();
                writer.flush();
                System.out.println("write완료");
            }
        }

        //GameField에서 보낸 문자를 다른 클라이언트들에게 전송하는 메서드
        //이떄는 다른 클라이언트들에게 전송할 때 "Game:"을 더해서 전송
        private void gamewordbroadcast(String message) throws IOException{
            clients.removeIf(Socket::isClosed);
            for(Socket client : clients) {
                System.out.println("게임브로드캐스트 시작");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                writer.write("Game:"+ message);
                writer.newLine();
                writer.flush();
            }
        }

        //        private void chatbroadcast(String message){
//            clients.removeIf(Socket::isClosed); // Socket이 닫혀있다면 clients에서 삭제
//            for (Socket client : clients) {
//                try {
//                    PrintWriter out = new PrintWriter(client.getOutputStream());
//                    out.println(message);
//                    out.flush();
//                }
//                catch (IOException e){
//                }
//            }
//        }

    }

    public static void main(String[] args) {
        new EndToEndServer();
    }
}

