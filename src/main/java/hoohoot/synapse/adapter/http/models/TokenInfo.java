package hoohoot.synapse.adapter.http.models;

public class TokenInfo {
  private String email;
  private String preferedUserName;
  private boolean isMatrixUser;

  public TokenInfo(String email, String preferedUserName, boolean isMatrixUser) {
    this.email = email;
    this.preferedUserName = preferedUserName;
    this.isMatrixUser = isMatrixUser;
  }

  public String getEmail() {
    return email;
  }

  public String getPreferedUserName() {
    return preferedUserName;
  }

  public boolean isMatrixUser() {
    return isMatrixUser;
  }
}
