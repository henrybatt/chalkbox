package chalkbox.stages;

import com.google.gson.annotations.SerializedName;

public enum Status {
    @SerializedName("passed")
    PASSED,
    @SerializedName("failed")
    FAILED,
}
