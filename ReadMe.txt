Pruning Algorithm(Iteration Depth of Pruning):
Outer loop continues iteration until no more improvement on accuracy.

Inner loop follows the algorithm on the lecture slide. 
- Iterate through each internal node using Breadth First Search. 
- Every time you visit a node, you set the node as a terminal and compute accuracy. 
- We keep track of the best accuracy associated with best pruning node. 
- At the end of iteration, we compare the best accuracy with the 
accuracy of the original tree(previous best accuracy).
- If best accuracy associated with the best pruning node is higher or equal
to the accuracy of the original tree, we prune the subtree of the best 
pruning node. 
- Otherwise we don't prune.

Breaking ties:
Within an iteration, if pruning subtree of node X and pruning of subtree of 
node Y produces the same best accuracy, we prune a node in order of Breadth First
Search.If we visit node X first, we prune the subtree of node X.   

Accuracy before pruning: 0.68098
Accuracy after pruning: 0.73620


 