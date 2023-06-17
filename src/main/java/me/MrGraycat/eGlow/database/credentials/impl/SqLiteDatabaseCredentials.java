package me.mrgraycat.eglow.database.credentials.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrgraycat.eglow.database.credentials.DatabaseCredentials;

@Getter @AllArgsConstructor
public class SqLiteDatabaseCredentials implements DatabaseCredentials {

    private final String databasePath;
    
}
