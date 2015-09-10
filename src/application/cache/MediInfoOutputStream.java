package application.cache;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class MediInfoOutputStream extends ObjectOutputStream {

    public MediInfoOutputStream(OutputStream out) throws IOException {
        super(out);
        enableReplaceObject(true);
    }

    protected MediInfoOutputStream() throws IOException, SecurityException {
        super();
        enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
        if (obj.getClass().getName().startsWith("org.xbmc.kore.jsonrpc.type")) {
            return new DefaultSerialize(obj);
        }else {
        	return super.replaceObject(obj);
        }
    }

}