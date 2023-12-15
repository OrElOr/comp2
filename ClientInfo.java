package WordChain;

import java.io.Serializable;

//*******************************************************************
// # 01
//*******************************************************************
// Name : ClientInfo
// Type : class
// Description :  Client의 이름과 정보를 저장할 class
//                생성자와 내부 데이터 get, set, up 동작이 구현되어 있다.
//*******************************************************************
public class ClientInfo implements Serializable {
    private String username;
    private int score;

    public ClientInfo(String username) {
        this.username = username;
        this.score = 0;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void upScore(int score) {
        this.score += score;
    }

    public int getScore() {
        return score;
    }

    public String toString() {
        return "이름 : "+ username +" / 점수 : "+ score;
    }
}

