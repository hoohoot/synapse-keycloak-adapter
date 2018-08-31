package hoohoot.synapse.adapter.models;

public class UserInfoDigest {
  private String email;
  private String preferedUserName;
  private boolean isMatrixUser;

  public UserInfoDigest(String email, String preferedUserName, boolean isMatrixUser) {
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
