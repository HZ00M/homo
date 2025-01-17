package com.homo.core.facade.relational.schema;

import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class IdentifierSchema {
    private final String text;
    private final boolean isQuoted;
    public static IdentifierSchema toIdentifier(String text) {
        if ( StringUtils.isEmpty( text ) ) {
            return null;
        }
        final String trimmedText = text.trim();
        if ( isQuoted( trimmedText ) ) {
            final String bareName = trimmedText.substring( 1, trimmedText.length() - 1 );
            return new IdentifierSchema(bareName, true);
        }
        else {
            return new IdentifierSchema(trimmedText, false);
        }
    }

    public static IdentifierSchema toIdentifier(String text, boolean quote) {
        if ( StringUtils.isEmpty( text ) ) {
            return null;
        }
        final String trimmedText = text.trim();
        if ( isQuoted( trimmedText ) ) {
            final String bareName = trimmedText.substring( 1, trimmedText.length() - 1 );
            return new IdentifierSchema(bareName, true);
        }
        else {
            return new IdentifierSchema(trimmedText, quote);
        }
    }
    public static IdentifierSchema quote(IdentifierSchema identifier) {
        return identifier.isQuoted()
                ? identifier
                : IdentifierSchema.toIdentifier( identifier.getText(), true );
    }
    public static boolean isQuoted(String name) {
        return ( name.startsWith( "`" ) && name.endsWith( "`" ) )
                || ( name.startsWith( "[" ) && name.endsWith( "]" ) )
                || ( name.startsWith( "\"" ) && name.endsWith( "\"" ) );
    }
    public IdentifierSchema(String text, boolean quoted) {
        if ( StringUtils.isEmpty( text ) ) {
            throw new RuntimeException( "Identifier text cannot be null" );
        }
        if ( isQuoted( text ) ) {
            throw new RuntimeException( "Identifier text should not contain quote markers (` or \")" );
        }
        this.text = text;
        this.isQuoted = quoted;
    }

    protected IdentifierSchema(String text) {
        this.text = text;
        this.isQuoted = false;
    }


    public String render() {
        return isQuoted
                ? '`' + getText() + '`'
                : getText();
    }
}
