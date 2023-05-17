leftobject.common.child1 = 'dot notation commmon value among all environments'
rightobject.common.child1 = '. notation commmon value among all environments'

environments {
    basic {
        leftobject {
            parentA {
                child1 = 1
            }
            parentB {
                child1 = 1
            }
        }
        rightobject {
            parentA {
                child1 = 1
            }
            parentB {
                child1 = 1
                child2 = 'something different'     // this is the only difference
            }
        }
    }

    moderate {
        leftobject {
            parentA {
                child1 = 1
                child2 = 2
            }
            parentB {
                child1 = 1
                child2 = 2
                // expect missing Child3B
            }
            parentC {           // expect this tree missing in left
                childTree1 {
                    childNode1 = '3 levels deep?'
                }
                childTree2 {
                    childNode2 {
                        grandchildNode1 = 'four levels deep?'
                    }
                }
            }
            parentListD = [2, 4, 6, 8]         // expect this is missing in right
            // expect missing parentListE from right
        }
        rightobject {
            parentA {
                child1 = 1
                child2 = 'two'
            }
            parentB {
                child1 = 1
                child2 = 2
                Child3B = 'something different'     // expect this missing in left
            }
            // missing parentC TREE
            // expect missing parentListD from left
            parentListE = [2, 4, [foo: '1', bar: '2']] // expect this is missing in right

        }
    }

    advanced {
        leftobject {
            parentA {
                child1 = 1
                child2 = 2          // note: 2 here, 'two' in rightobject
            }
            parentB {
                child1 = 1
                child2 = 2
                // note: expect missing Child3B
            }
            parentC {           // note: now this structure matches, but values differ
                childTree1 {
                    childNode1 = 'track child value differences'
                }
                childTree2 {
                    childNode2 {
                        grandchildNode1 = 'ignore grandchild value differences'
                    }
                }
            }
            parentListD = [2, 4, 6, 8]         // note: expect this is missing in right
            // note: expect missing parentListE from right
        }

        rightobject {
            parentA {
                child1 = 'one'
                child2 = 'two'
            }
            parentB {
                child1 = 1
                child2 = 2
            }
            parentC {           // note: expect this tree missing in left
                childTree1 {
                    childNode1 = 'track child value differences - this should show as a difference'
                }
                childTree2 {
                    childNode2 {
                        grandchildNode1 = 'ignore grandchild value differences -- this should NOT show as a difference'
                    }
                }
            }
            // note: missing parentC TREE
            // note: expect missing parentListD from left
            parentListE = [2, 4, [foo: '1', bar: '2']] // note: expect this is missing in right

        }
    }

}
