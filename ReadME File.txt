ReadME File |
------------
Honor code: All group members were
present and contributing during all work on this project
Middlebury Honor Code: We have not given nor received unauthorized aid on this assignment.

Names: John Bermudez and Daniela Delgado

Names of files required to run the program: SudokuPlayer.java

Known bugs: --none--

CustomizedSolver = (Forward Checking with AC3)

Our custom solver involves using the forward checking algorithm with the AC3 algorithm. This algorithm is slower than AC3 backtracking algorithm but is still
capable of solving a sudoku board. For example, with our custom solver's easy sudoku board, it was able to solve it within ~530 recursive operations. Whereas in the
medium and hard sudoku boards, it stays running for too long that there is not enough heap space for further calculation. To make our forward checking
algorithm run faster, combining it with the most constrained variable would allow the forward check to correctly choose the most efficient path instead
of the longer one.

Outside sources: Smith Gakuya, StackOverFlow, Geeks4Geeks, ChatGPT (to clarify the what certain objects do such as ArrayList<Integer>[])

