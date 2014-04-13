package com.bagri.xdm.access.coherence.pof;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

import java.io.IOException;
import java.util.Date;

/**
 * Date: 8/13/12 6:23 PM
 */
public class DatePofSerializer implements PofSerializer {

    @Override
    public Object deserialize(PofReader pofReader) throws IOException {
        Date date = new Date(pofReader.readLong(0));
        pofReader.readRemainder();
        return date;
    }

    @Override
    public void serialize(PofWriter pofWriter, Object date) throws IOException {
        pofWriter.writeLong(0, ((Date) date).getTime());
        pofWriter.writeRemainder(null);
    }

}