package com.rusty.openaiapigps.config.datasource;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
public class LookupKey {

    private final String driver;
    private final String url;
    private final String userName;
    private final String password;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        LookupKey lookupKey = (LookupKey) obj;
        return Objects.equals(url, lookupKey.url) &&
                Objects.equals(userName, lookupKey.userName) &&
                Objects.equals(password, lookupKey.password);
    }


    @Override
    public int hashCode() {
        return Objects.hash(url, userName, password);
    }


    @Override
    public String toString() {
        return "LookupKey{url='" + url + "', userName='" + userName + "', password='" + password + "'}";
    }
}
