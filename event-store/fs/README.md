# File System

This is an implementation of the event store where all the data is stored in files.

The main goal of this implementation is to serve as a reference point for other persistent implementations.
It will still strive to be correct, reasonably optimized and support concurrent writers, though, so you may be able to use it on a real project if you see fit.

## General design

TODO

## File Layout

TODO

* pos file
```
| wal_addr: int64 | ... |
```
* wal file
```
| wal_entry_size: int32, wal_entry: byte[], wal_entry_size: int32, position: int64 | ... |
```

* wall entry - it's a class with the following properties, which is serialized using `protobuf`. 
```
type: string
id: string
version: int32
payload: byte[]
```

* stream file
```
| wal_addr: int64 | ... |
```
