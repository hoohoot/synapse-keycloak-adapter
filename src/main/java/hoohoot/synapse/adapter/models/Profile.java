package hoohoot.synapse.adapter.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Profile {
  @JsonProperty("display_name")
  private String displayName;
  @JsonProperty("three_pids")
  private List<ThreePid> threePids;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<ThreePid> getThreePids() {
    return threePids;
  }

  public void setThreePids(List<ThreePid> threePids) {
    this.threePids = threePids;
  }
}
