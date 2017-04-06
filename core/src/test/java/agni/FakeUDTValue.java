package agni;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;

public class FakeUDTValue {
    public static UDTValue get() throws Exception {
        Class<UserType> d = UserType.class;
        Constructor<UserType> dd = d.getDeclaredConstructor(String.class, String.class, boolean.class, Collection.class, ProtocolVersion.class, CodecRegistry.class);
        dd.setAccessible(true);
        Class<UDTValue> c = UDTValue.class;
        Constructor<UDTValue> cc = c.getDeclaredConstructor(d);
        cc.setAccessible(true);
        return cc.newInstance(dd.newInstance("", "", java.lang.Boolean.TRUE, Collections.EMPTY_LIST, ProtocolVersion.V5, CodecRegistry.DEFAULT_INSTANCE));
    }
}
