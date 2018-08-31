package hoohoot.synapse.adapter.models;

public class AdapterResponse {
  private boolean success;
  private String mxid;
  private Profile profile;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMxid() {
    return mxid;
  }

  public void setMxid(String mxid) {
    this.mxid = mxid;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }
}
