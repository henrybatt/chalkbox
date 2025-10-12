package chalkbox.stages;

import com.google.gson.annotations.SerializedName;

public enum Visibility {
    @SerializedName("hidden")
    HIDDEN,
    @SerializedName("after_due_date")
    AFTER_DUE_DATE,
    @SerializedName("after_published")
    AFTER_PUBLISH,
    @SerializedName("visible")
    VISIBLE
}
