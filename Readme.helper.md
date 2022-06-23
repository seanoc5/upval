# Notes on Helper Class (flattening and navigation)

## All Maps
```
[
    a: [foo:1, bar,2],
    b: [bizz: buzz],
    c: leafNode,
]
```
## Get Values
- path: /a/foo => 1
- path: /a/bar => 2
- path /b/bizz => buzz
- path /c  => leafNode
- path /a/fubar => null

## Set Values
- Existing entry
  - set('/a/foo', 'first')
- New entry
  - set('/a/newKey', 'newValue')
    - get a
    - find newKey missing: 
      - create it
      - if is a leaf node, set it to newValue
  - set('/a/newParent/newLeaf', 'newValue')
    - find newParent missing
      - create it
      - it is NOT a leaf node, but is a map, create new map
      - recurse to find newLeaf, set newParent[newLeaf] = newValue


### Maps with collections
```
[
    a:[foo:1, bar:2]
    b:['first', 'second']
]
```
- path: /b/0 -> 'first'
- path: /b/2 -> null

