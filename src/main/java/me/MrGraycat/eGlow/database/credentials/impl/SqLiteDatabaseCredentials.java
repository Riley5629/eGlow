package me.MrGraycat.eGlow.database.credentials.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.MrGraycat.eGlow.database.credentials.DatabaseCredentials;

@Getter @AllArgsConstructor
public class SqLiteDatabaseCredentials implements DatabaseCredentials {

    private final String databasePath;
    
}
