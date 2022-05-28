package com.lucidworks.ps.compare

/**
 * encapsulate comparison results
 * - CompareType
 - Ids/keys
 - leftOnly
 - RightOnly
 - Shared
 - Differences
 */
class Results {
    String compareType
    List leftOnlyIds
    List rightOnlyIds
    List sharedIds
    List<Difference> differences
}
