package com.cometkaizo.io;

import com.cometkaizo.io.data.CompoundData;

public interface DataSerializable {
    CompoundData write();
    void read(CompoundData data);
}
