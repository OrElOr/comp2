package WordChain;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

//*******************************************************************
// # 04-01
//*******************************************************************
// Name : WordChainServer
// Type : Class
// Description :  Client와의 소켓통신을 담당하고,
//                dictionary.txt를 읽어서 끝말잇기에 사용될 단어를 불러오고 두음법칙을 적다.킨시ㅇ용
//                끝말잇기에 사용된 단어를 stack형태로 저장 및 관리한다.
//                message를 주고 받는 기능과 message 종류에 따른 관련 기능들을 가지고 있다.
//*******************************************************************
public class WordChainServer {
    private static Map<Socket, ClientInfo> clients = new HashMap<>();
    private static List<String> wordList = new ArrayList<>();//단어사전 리스트
    private static Stack<String> usedWords = new Stack<>();//사용한 단어 스택
    private static Map<Character, Character> duem = new HashMap<>(); //두음법칙 적용 단어
    private static Set<Character> onePunch = new HashSet<>();//한방문자 집합
    private ServerSocket serverSocket;


    //*******************************************************************
    // # 04-01-01
    //*******************************************************************
    // Name : WordChainServer
    // Type : 생성자
    // Description :  readWordList와 setDuem을 초기화하고 wordList에서 임의의 단어를 골라
    //                usedWords에 push하고 서버소켓을 만든 후 acceptClient()를 호출한다.
    //*******************************************************************
    public WordChainServer() throws IOException {
        readWordList();
        setDuem();
        setOnePunch();
        //초기 끝말잇기 단어 설정
        usedWords.push(wordList.get( (int)(Math.random() * (wordList.size()-1)) ));

        serverSocket = new ServerSocket(5000);
        acceptClient();
    }

    //*******************************************************************
    // # 04-01-02
    //*******************************************************************
    // Name : acceptClient
    // Type : Method
    // Description :  While문을 통해 새로운 Client의 접속을 기다린다.
    //*******************************************************************
    public void acceptClient() throws IOException{
        while (true) {
            Socket clientSocket= null;
            try {
                clientSocket = serverSocket.accept();

                ObjectInputStream objectReader = new ObjectInputStream(clientSocket.getInputStream());

                ClientInfo clientInfo = null;
                try {
                    clientInfo = (ClientInfo) objectReader.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                clients.put(clientSocket, clientInfo);
                System.out.println("클라이언트 연결됨: " + clientInfo.getUsername());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            } catch (SocketException e) {
                clientSocket.close();
            }
        }
    }


    //*******************************************************************
    // # 04-01-03
    //*******************************************************************
    // Name : readWordList
    // Type : Method
    // Description :  dictionary.txt을 한줄씩 읽어서 wordList에 추가한다.
    //*******************************************************************
    public void readWordList() {
        try(BufferedReader in = new BufferedReader(new FileReader("src/WordChain/dictionary.txt"))){
            String str;
            while((str=in.readLine())!=null){
                wordList.add(str);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // # 04-01-04
    //*******************************************************************
    // Name : setDuem
    // Type : Method
    // Description :  두음법칙 적용전 글자를 Key, 적용후 글자를 Value에 해당하도록 HashMap 형태로 저장한다.
    //*******************************************************************
    public void setDuem() {
        char[] basic = {'롭','뉵','력', '랄','랏','림','률','랴','녁','릿','룰', '례','릇','렁','뢰',
                '롄','룡','니','냐','녀','뇨','뉴','라','래','로','르','류','료','리','려'};
        char[] change = {'놉','육','역', '날','낫','임','율','야','역','잇','눌','예','늣','넝','뇌',
                '옌','용','이','야','여','요','유','나','내','노','느','유','요','이','여'};
        for (int i=0; i<basic.length; i++) {
            duem.put(basic[i],change[i]);
        }
    }

    //*******************************************************************
    // # 04-01-05
    //*******************************************************************
    // Name : setOnePunch
    // Type : Method
    // Description :  이을수 없는 문자를 모은 집합을 만든다.
    //*******************************************************************
    public void setOnePunch() {
        char[] c = {'릇','쁨','름','값','려','력','륨','켓','녘','륨','섯','껑','귿','뇨','즘','듭',
                '봇','늬','릎','슘','또','뺌','엌','료','례','늄','션','츠','갗','튬','탉','득','앗','뢰','빠','퓸','븀','듐','뮴'};
        for (int i=0; i<c.length; i++) {
            onePunch.add(c[i]);
        }
    }

    public static void main(String[] args) {
        try {
            new WordChainServer();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    //*******************************************************************
    // # 04-02
    //*******************************************************************
    // Name : ClientHandler
    // Type : Class
    // Description : Server의 내부 클래스, 클라이언트와 소켓 통신을 담당한다.
    //               끝말잇기 로직 수행, 클라이언트 관리 등의 method를 갖고 있는 class
    //*******************************************************************
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


        //*******************************************************************
        // # 04-02-01
        //*******************************************************************
        // Name : run
        // Type : Method
        // Description : reader를 통해 받은 message를 구분하여 분류에 따라 Brodcast한다.
        //               새로운 Client가 접속한 경우에는 stack의 제일 위에 값을 TargetWord로 설정 할 수 있도록해준다.
        //*******************************************************************
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {

                    if (message.startsWith("Game:")) { // InputWord에서 온 문자인 경우
                        logic(message.replace("Game:", ""));
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
        

        //*******************************************************************
        // # 04-02-02
        //*******************************************************************
        // Name : logic
        // Type : Method
        // Description : 끝말잇기 로직을 수행하는 메소드이다.
        //               사용 가능한 단어인지, 한방단어인지등을 검사한다
        //*******************************************************************
        public void logic(String word) {
            //targetWord 기존단어, word 유저가 입력한 단어
            String targetWord = usedWords.peek();

            char lastTargetWord = targetWord.charAt(targetWord.length()-1);
            char firstWord = word.charAt(0);

            if (duem.containsKey(lastTargetWord)){ // 두음법칙이 적용되는 경우
                //두음법칙을 적용하여 바꾼단어
                char changeLastTargetWord = duem.get(lastTargetWord);

                if (!wordList.contains(word)) {
                    sendMessageToClient("--사용할 수 없는 단어입니다.--");
                } else if(usedWords.contains(word)){
                    sendMessageToClient("--이미 사용된 단어입니다.--");
                } else if(onePunch.contains(firstWord)){
                    sendMessageToClient("--한방 단어는 사용할 수 없습니다.--");
                } else if(!(lastTargetWord == firstWord) && !(changeLastTargetWord == firstWord)){ // 두음법칙 적용 전/후 단어와 비교
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
                } else if(onePunch.contains(firstWord)){
                    sendMessageToClient("--한방 단어는 사용할 수 없습니다.--");
                } else if (!(lastTargetWord == firstWord)) {
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


        //*******************************************************************
        // # 04-02-03
        //*******************************************************************
        // Name : waitClient
        // Type : Method
        // Description : Client가 정답을 맞춘 경우 정답자가 연속으로 정답을 맞추는 것을 방지하기위해
        //           Client를 멈추게하는 신호를 보내는 Method
        //*******************************************************************
        private void waitClient() {
            try {
                writer.write("WaitClient:");
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //*******************************************************************
        // # 04-02-04
        //*******************************************************************
        // Name : sendMessageToClient
        // Type : Method
        // Description :  단일 Client에게 message를 보내는 Method
        //*******************************************************************
        private void sendMessageToClient(String message) {
            try {
                writer.write("Chat:"+ message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //*******************************************************************
        // # 04-02-05
        //*******************************************************************
        // Name : sendMessageToClient
        // Type : Method
        // Description :  모든 Client에게 chat를 보내는 Method
        //                Chat: 으로 시작하는 문장을 보낸다.
        //*******************************************************************
        private void chatBroadcast(String chat) throws IOException{
            for(Socket client : clients.keySet()) {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                    writer.write(chat);
                    writer.newLine();
                    writer.flush();
            }
        }


        //*******************************************************************
        // # 04-02-06
        //*******************************************************************
        // Name : targetWordBroadcast
        // Type : Method
        // Description :  logic을 통과한 단어를 Game:를 붙여 보내고, 정답자의 username과 점수를 chatArea에 띄운다.
        //*******************************************************************
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
            }
        }

        //*******************************************************************
        // # 04-02-07
        //*******************************************************************
        // Name : checkScore
        // Type : Method
        // Description :  score가 10점을 넘긴 client가 있는 경우(승리조건을 만족한 경우)
        //                승자에게 "GameEnd:Win:"를 보내 승리 메세지를 띄어주고
        //                패자에게 승자의 username과 패배 메세지를 띄워준다.
        //*******************************************************************
        private void checkScore() throws IOException{
            for(Socket client : clients.keySet()) {
                int score = clients.get(client).getScore();
                if (score >= 10) {
                    BufferedWriter win = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                    win.write("GameEnd:Win:");
                    win.newLine();
                    win.flush();

                    String winner = clients.get(client).getUsername();
                    for(Socket cli : clients.keySet()) {
                        int sc = clients.get(cli).getScore();
                        if(sc < 10) {
                            BufferedWriter lose = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));

                            lose.write("Chat:"+ winner +"님이 우승하였습니다!");
                            lose.newLine();
                            lose.flush();

                            lose.write("GameEnd:Lose:");
                            lose.newLine();
                            lose.flush();
                        }
                    }
                    break;
                }
            }
        }


        //*******************************************************************
        // # 04-02-09
        //*******************************************************************
        // Name : closeClient
        // Type : Method
        // Description :  서버에 Client 종료 메세지를 띄우고
        //                clientScocket이 열려있는 경우 reader,writer,clientScocket을 닫는다.
        //*******************************************************************
        private void closeClient() {
            if (!clientSocket.isClosed()) {
                try {
                    System.out.println("클라이언트 연결종료 : "+ clients.get(clientSocket).getUsername());
                    clients.remove(clientSocket);

                    reader.close();
                    writer.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}

