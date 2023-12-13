package WordChain;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class WordChainServer {
    private static Map<Socket, ClientInfo> clients = new HashMap<>();
    private static List<String> wordList = new ArrayList<>();//단어사전 리스트
    private static Stack<String> usedWords = new Stack<>();//사용한 단어 스택
    private static Map<String,String> duem = new HashMap<>(); //두음법칙 적용 단어
    private ServerSocket serverSocket;
    private ObjectInputStream objectReader;

    public WordChainServer() {
        readWord();

        String[] basic = {"롭","뉵","력", "랄","랏","림","률","랴","녁","릿","룰","례","릇","렁","뢰",
                    "롄","룡","니","냐","녀","뇨","뉴","라","래","로","르","류","료","리","려"};
        String[] change = {"놉","육","역", "날","낫","임","율","야","역","잇","눌","예","늣","넝","뇌",
                    "옌","용","이","야","여","요","유","나","내","노","느","유","요","이","여"};
        for (int i=0;i<30;i++) {
            duem.put(basic[i],change[i]);
        }

        //초기 단어 설정
        usedWords.push(wordList.get(0));

        try {
            serverSocket = new ServerSocket(12345);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                objectReader = new ObjectInputStream(clientSocket.getInputStream());

                ClientInfo clientInfo = null;
                try {
                    clientInfo = (ClientInfo) objectReader.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

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
    public void readWord() {
        try(BufferedReader in = new BufferedReader(new FileReader("src/WordChain/dictionary.txt"))){
            String str;
            while((str=in.readLine())!=null){
                wordList.add(str);
            }
            Collections.shuffle(wordList);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new WordChainServer();
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader reader;
        private BufferedWriter writer;

        public ClientHandler(Socket clientSocket) {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                this.clientSocket = clientSocket;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {

                    if (message.startsWith("Game:")) { // InputWord에서 온 문자인 경우
                        String tryWord = message.replace("Game:", "");
                        logic(tryWord);
                    } else if (message.startsWith("Chat:")) {
                        chatBroadcast(message);
                    } else if (message.equals("SetInitTargetWord:")) {
                        writer.write("SetInitTargetWord:"+ usedWords.peek());
                        writer.newLine();
                        writer.flush();
                    } else if (message.equals("Disconnect:")) {
                        closeClient();
                    }
                }
            } catch (IOException e) {
                closeClient();
            }
        }

        // 끝말잇기 조건을 구현한 메서드
        public void logic(String word) {
            //targetWord 기존단어, word 유저가 입력한 단어
            String targetWord = usedWords.peek();

            String lastTargetWord = ""+ targetWord.charAt(targetWord.length()-1);
            String firstWord = ""+ word.charAt(0);

            if (duem.containsKey(lastTargetWord)){ // 끝 글자가 두음법칙이 적용되는 경우
                //두음법칙을 적용하여 바꾼단어
                String changeLastTargetWord = duem.get(lastTargetWord);

                if (!wordList.contains(word)) {
                    sendMessageToClient("--사용할 수 없는 단어입니다.--");
                } else if(usedWords.contains(word)){
                    sendMessageToClient("--이미 사용된 단어입니다.--");
                } else if(!lastTargetWord.equals(firstWord) && !changeLastTargetWord.equals(firstWord)){ // 두음법칙 적용 전/후 단어와 비교
                    sendMessageToClient("--틀렸습니다.--");
                } else {
                    usedWords.push(word);
                    waitClient();
                    clients.get(clientSocket).upScore(word.length());
                    try {
                        targetWordBroadcast(word);
                        checkScore();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (!wordList.contains(word)) {
                    sendMessageToClient("--사용할 수 없는 단어입니다.--");
                } else if (usedWords.contains(word)) {
                    sendMessageToClient("--이미 사용된 단어입니다.--");
                } else if (!lastTargetWord.equals(firstWord)) {
                    sendMessageToClient("--틀렸습니다.--");
                } else {
                    usedWords.push(word);
                    waitClient();
                    clients.get(clientSocket).upScore(word.length());
                    try {
                        targetWordBroadcast(word);
                        checkScore();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 클라이언트에게 기다리라는 신호를 보내는 메소드
        private void waitClient() {
            try {
                writer.write("WaitClient:");
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 하나의 클라이언트에게 메세지 보내는 메소드
        private void sendMessageToClient(String message) {
            try {
                writer.write("Chat:"+ message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 각 클라이언트들에게 채팅을 전송하는 메소드
        private void chatBroadcast(String chat) throws IOException{
            for(Socket client : clients.keySet()) {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                    writer.write(chat);
                    writer.newLine();
                    writer.flush();
                    //writer.close();
            }
        }

        // 각 클라이언트들에게 바뀐 targetword,와 점수를 알리는 메소드
        private void targetWordBroadcast(String targetWord) throws IOException {
            for (Socket client : clients.keySet()) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                writer.write("Game:" + targetWord);
                writer.newLine();
                writer.flush();

                writer.write("Chat:"+ clients.get(clientSocket).getUsername() + "님이 정답을 맞췄습니다! " +clients.get(clientSocket).getUsername()+
                        "님의 점수는 "+ clients.get(clientSocket).getScore() +"(+"+ targetWord.length() +")점!");
                writer.newLine();
                writer.flush();
                //writer.close();
            }
        }

        //클라이언트들 점수 확인하고 목표점수 도달 시 승/패 알리는 메소드
        private void checkScore() throws IOException{
            for(Socket client : clients.keySet()) {
                int score = clients.get(client).getScore();
                if (score >= 2) {
                    BufferedWriter win = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                    win.write("GameEnd:Win:");
                    win.newLine();
                    win.flush();
                    //win.close();

                    String winner = clients.get(client).getUsername();
                    for(Socket cli : clients.keySet()) {
                        int sc = clients.get(cli).getScore();
                        if(sc < 2) {
                            BufferedWriter lose = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));

                            lose.write("Chat:"+ winner +"님이 우승하였습니다!");
                            lose.newLine();
                            lose.flush();

                            lose.write("GameEnd:Lose:");
                            lose.newLine();
                            lose.flush();
                            //lose.close();
                        }
                    }
                    break;
                }
            }
        }

        private void closeClient() {
            if (!clientSocket.isClosed()) {
                try {
                    clients.remove(clientSocket);

                    reader.close();
                    writer.close();
                    clientSocket.close();
                    System.out.println("클라이언트 소켓 닫힘");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
