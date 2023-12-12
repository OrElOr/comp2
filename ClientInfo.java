package WordChain;

import java.io.Serializable;

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

    public int getScore() {
        return score;
    }

    public String toString() {
        return "이름 : "+ username +" / 점수 : "+ score;
    }
}
