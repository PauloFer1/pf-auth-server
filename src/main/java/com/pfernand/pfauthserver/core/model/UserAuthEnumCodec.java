package com.pfernand.pfauthserver.core.model;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;


public class UserAuthEnumCodec implements Codec<UserAuthSubject> {

    @Override
    public void encode(final BsonWriter writer, final UserAuthSubject value, final EncoderContext encoderContext) {
        writer.writeInt32(value.ordinal());
    }

    @Override
    public Class<UserAuthSubject> getEncoderClass() {
        return UserAuthSubject.class;
    }

    @Override
    public UserAuthSubject decode(final BsonReader reader, final DecoderContext decoderContext) {
        final String ordinal = reader.readString();
        switch (ordinal){
            case "emp":
                return UserAuthSubject.EMPLOYEE;
            case "cst":
                return UserAuthSubject.CUSTOMER;
            case "vst":
                return UserAuthSubject.VISITOR;
            case "ext":
                return UserAuthSubject.EXTERNAL;
            case "svc":
                return UserAuthSubject.SERVICE;
            default:
                return UserAuthSubject.VISITOR;
        }
    }
}
