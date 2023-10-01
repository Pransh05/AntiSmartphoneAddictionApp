package com.example.antismartphoneaddictionapp.Models;

public class Preference {
    private String  preferenceId,preferenceName;
    private boolean isPreferenceSelected;

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getPreferenceName() {
        return preferenceName;
    }

    public void setPreferenceName(String preferenceName) {
        this.preferenceName = preferenceName;
    }

    public boolean isPreferenceSelected() {
        return isPreferenceSelected;
    }

    public void setPreferenceSelected(boolean preferenceSelected) {
        isPreferenceSelected = preferenceSelected;
    }
}
