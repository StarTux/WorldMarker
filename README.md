# WorldMarker

Manage custom persistent ids in blocks, items, entities.

## Access

### Marking a block
```java
BlockMarker.getBlock(block).setId("myplugin:myid");
```

### Finding a block

For example, in a block break handler:
```java
if (BlockMarker.getBlock(block).hasId("myplugin:myid")) {
    ...
}
```

### Ticking a block
An event will be called on each tick for every loaded chunk containing
custom blocks. The `getBlocksWithId` method within returns a
`Stream<MarkBlock` which can be operated on easily.
```java
@EventHandler
void onMarkChunkTick(MarkChunkTickEvent event) {
    event.getBlocksWithId("myplugin:myid").forEach(this::onTickMyBlock);
}
```

### Persistent data cache
The framework stores persistent data on every block, chunk and world
which are saved intelligently. The persistent class must implement the
provided interface and has to be easily (de)serializable via Gson.
```java
class MyData extends com.cavetale.worldmarker.Persistent {
    ...
}
MyData data = BlockMarker.getBlock(block).getPersistent("myplugin:myid", MyData.class, MyData::new);
```
It is not required for `getPersistent` to use the same key as the
custom block id. By convention, doing so will clear the persistent
data whenever the id is reset.

The `save()` method must always be called when a change was made which
needs to be saved. Actual writing to disk is deferred by the plugin
automatically.