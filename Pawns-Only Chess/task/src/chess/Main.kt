package chess

import java.lang.Math.abs
import java.time.Year

enum class State() { WRONG_INPUT, NO_PIECE, CONTINUE, EXIT, WHITE_WON, BLACK_WON, DRAW}

enum class StateMove() { ILLEGAL_MOVE, LEGAL_MOVE}

enum class TypeMove() { MOVEMENT, MOVEMENT_DOUBLE, CAPTURE, EN_PASSANT}

class ChessBoard() {
    var turnCount = 0
    var auth = 0
    var state: State = State.CONTINUE
    val board: MutableList<Piece> = mutableListOf<Piece>()
    val players: MutableList<Player> = mutableListOf<Player>()
    val files = ('a'..'h').toList()
    val ranks = (1..8).toList()

    fun isFree(x: Char, y: Int): Boolean {
        return !board.any { it.x == x && it.y == y }
    }

    fun isInBoundaries(x: Char, y: Int): Boolean {
        return (x in 'a'..'h' && y in 1..8)
    }

    fun isOccupiedByOpponent(x: Char, y: Int, mySign: Char): Boolean {
        return board.any { it.x == x && it.y == y && it.sign != mySign }
    }

    fun isOccupiedByPlayer(x: Char, y: Int, mySign: Char): Boolean {
        return board.any { it.x == x && it.y == y && it.sign == mySign }
    }

    fun isOccupiedByEnPassantOpponent(x: Char, y: Int, mySign: Char): Boolean {
        return board.any {  it.x == x && it.y == y &&
                           it.sign != mySign &&
                           (it as Pawn).movedTwoSpaces &&
                           it.lastMoveTurn == turnCount }
    }

    open inner class Piece(var x: Char, var y: Int, val sign: Char) {
        var numberOfMoves = 0
        var lastMoveTurn = 0
        var availableMoves = mutableListOf<String>()
        var availableMoveTypes = mutableListOf<TypeMove>()

    }

    open inner class Pawn(x: Char, y: Int, sign: Char): Piece(x, y, sign) {
        var movedTwoSpaces: Boolean = false

        fun movePiece(inputString: String): StateMove {
            val indexOfMove = this.availableMoves.indexOf(inputString)
            if (indexOfMove == -1) return StateMove.ILLEGAL_MOVE
            val (xStart, yStartChar, xTo, yToChar) = inputString.toCharArray()
            val yTo = yToChar.digitToInt()
            val yStart = yStartChar.digitToInt()


            if (this.availableMoveTypes[indexOfMove] == TypeMove.MOVEMENT_DOUBLE) {
                movedTwoSpaces = true
            }

            //Clear enemies
            if (this.availableMoveTypes[indexOfMove] == TypeMove.CAPTURE) {
                board.removeAll {it.x == xTo && it.y == yTo }
            }

            //Clear en passant enemies
            if (this.availableMoveTypes[indexOfMove] == TypeMove.EN_PASSANT) {
                board.removeAll {it.x == xTo && it.y == yStart }
            }

            numberOfMoves++
            lastMoveTurn = turnCount
            x = xTo
            y = yTo
            return StateMove.LEGAL_MOVE

        }
    }

    inner class WhitePawn(x: Char, y: Int): Pawn(x, y, 'W') {

        init {
            this.computeAvailableMoves()
        }

        fun computeAvailableMoves() {
            this.availableMoves.clear()
            this.availableMoveTypes.clear()

            // Check if move by 1 forward is a good move
            if (this@ChessBoard.isFree(x, y + 1) && this@ChessBoard.isInBoundaries(x, y + 1)) {
                this.availableMoves.add("$x$y$x${y + 1}")
                this.availableMoveTypes.add(TypeMove.MOVEMENT)
            }
            // Check if move by 2 forward is a good move
            if (this@ChessBoard.isFree(x, y + 2) &&
                this@ChessBoard.isInBoundaries(x, y + 2) &&
                this.numberOfMoves == 0) {
                this.availableMoves.add("$x$y$x${y + 2}")
                this.availableMoveTypes.add(TypeMove.MOVEMENT_DOUBLE)
            }

            // Check if normal captures are ok
            if (this@ChessBoard.isInBoundaries(x + 1, y + 1) &&
                this@ChessBoard.isOccupiedByOpponent(x + 1, y + 1, this.sign)) {
                this.availableMoves.add("$x$y${x + 1}${y + 1}")
                this.availableMoveTypes.add(TypeMove.CAPTURE)
            }

            if (this@ChessBoard.isInBoundaries(x - 1, y + 1) &&
                this@ChessBoard.isOccupiedByOpponent(x - 1, y + 1, this.sign)) {
                this.availableMoves.add("$x$y${x - 1}${y + 1}")
                this.availableMoveTypes.add(TypeMove.CAPTURE)
            }


            // Check if en passant capture are ok
            if (this@ChessBoard.isFree(x + 1, y + 1) &&
                this@ChessBoard.isInBoundaries(x + 1, y + 1) &&
                this@ChessBoard.isOccupiedByEnPassantOpponent(x + 1, y, this.sign)) {
                this.availableMoves.add("$x$y${x + 1}${y + 1}")
                this.availableMoveTypes.add(TypeMove.EN_PASSANT)
            }

            if (this@ChessBoard.isFree(x - 1, y + 1) &&
                this@ChessBoard.isInBoundaries(x - 1, y + 1) &&
                this@ChessBoard.isOccupiedByEnPassantOpponent(x - 1, y, this.sign)) {
                this.availableMoves.add("$x$y${x - 1}${y + 1}")
                this.availableMoveTypes.add(TypeMove.EN_PASSANT)
            }

        }
    }

    inner class BlackPawn(x: Char, y: Int): Pawn(x, y, 'B') {

        init {
            this.computeAvailableMoves()
        }

        fun computeAvailableMoves() {
            this.availableMoves.clear()
            this.availableMoveTypes.clear()
            // Check if move by 1 forward is a good move
            if (this@ChessBoard.isFree(x, y - 1) && this@ChessBoard.isInBoundaries(x, y - 1)) {

                this.availableMoves.add("$x$y$x${y - 1}")
                this.availableMoveTypes.add(TypeMove.MOVEMENT)
            }
            // Check if move by 2 forward is a good move
            if (this@ChessBoard.isFree(x, y - 2) &&
                this@ChessBoard.isInBoundaries(x, y - 2) &&
                this.numberOfMoves == 0) {

                this.availableMoves.add("$x$y$x${y - 2}")
                this.availableMoveTypes.add(TypeMove.MOVEMENT_DOUBLE)
            }

            // Check if normal captures are ok

            if (this@ChessBoard.isInBoundaries(x + 1, y - 1) &&
                this@ChessBoard.isOccupiedByOpponent(x + 1, y - 1, this.sign)) {

                this.availableMoves.add("$x$y${x + 1}${y - 1}")
                this.availableMoveTypes.add(TypeMove.CAPTURE)
            }

            if (this@ChessBoard.isInBoundaries(x - 1, y - 1) &&
                this@ChessBoard.isOccupiedByOpponent(x - 1, y - 1, this.sign)) {

                this.availableMoves.add("$x$y${x - 1}${y - 1}")
                this.availableMoveTypes.add(TypeMove.CAPTURE)
            }

            // Check if en passant capture are ok
            if (this@ChessBoard.isFree(x + 1, y - 1) &&
                this@ChessBoard.isInBoundaries(x + 1, y - 1) &&
                this@ChessBoard.isOccupiedByEnPassantOpponent(x + 1, y, this.sign)) {

                this.availableMoves.add("$x$y${x + 1}${y - 1}")
                this.availableMoveTypes.add(TypeMove.EN_PASSANT)
            }

            if (this@ChessBoard.isFree(x - 1, y - 1) &&
                this@ChessBoard.isInBoundaries(x - 1, y - 1) &&
                this@ChessBoard.isOccupiedByEnPassantOpponent(x - 1, y, this.sign)) {

                this.availableMoves.add("$x$y${x - 1}${y - 1}")
                this.availableMoveTypes.add(TypeMove.EN_PASSANT)
            }

        }
    }

    inner class Player(val name: String, val color: Char) {
        fun takeTurn(): State {
            val inputPattern = Regex("""[a-h][1-8][a-h][1-8]""")
            println("$name's turn:")
            val input = readln()
            if (input == "exit") return State.EXIT
            if (!input.matches(inputPattern)) {
                println("Invalid Input")
                return State.WRONG_INPUT
            }
            val (x, y, xTo, yTo) = input.toCharArray()
            val indexPiece = board.indexOfFirst { it.x == x && it.y == y.digitToInt() && it.sign == color }
            if (indexPiece == -1) {
                if (color == 'W') {
                    println("No white pawn at $x$y")
                } else if (color == 'B') {
                    println("No black pawn at $x$y")
                }
                return State.NO_PIECE
            }
            val stateMove = (board[indexPiece] as Pawn).movePiece(input)
            for (i in board) {
                if (i is WhitePawn) {
                    i.computeAvailableMoves()
                }
                if (i is BlackPawn) {
                    i.computeAvailableMoves()
                }
            }
            if (stateMove == StateMove.ILLEGAL_MOVE) return State.WRONG_INPUT
            return State.CONTINUE
        }

        fun checkIfWonOrDraw(): State {
            // winning by arriving at the end
            if (this.color == 'W' &&
                board.any { it.y == 8 && it.sign == 'W'}) return State.WHITE_WON
            if (board.filterIsInstance<BlackPawn>().isEmpty()) return State.WHITE_WON

            if (this.color == 'B' &&
                board.any { it.y == 1 && it.sign == 'B'}) return State.BLACK_WON
            if (board.filterIsInstance<WhitePawn>().isEmpty()) return State.BLACK_WON

            if (board.flatMap { if (it.sign == 'W') it.availableMoves else mutableListOf<String>() }.isEmpty()) return State.DRAW
            if (board.flatMap { if (it.sign == 'B') it.availableMoves else mutableListOf<String>() }.isEmpty()) return State.DRAW
            return  State.CONTINUE
        }
    }

    fun initializeBoardOnlyPawnChess() {
        for (i in 'a'..'h') {
            board.add(WhitePawn(i,2))
            board.add(BlackPawn(i,7))
        }
    }

    fun printBoard() {
        val separator = "  " + "+---".repeat(8) + '+'
        var lastLine = "  "
        for (j in 'a'..'h') {
            lastLine += "  $j "
        }
        println(separator)
        for (i in 8 downTo 1) {
            print("$i | ")
            for (j in 'a'..'h') {
                val piece = board.find { it.y == i && it.x == j }
                print(piece?.sign ?: ' ')
                print(" | ")
            }
            println()
            println(separator)
        }
        println(lastLine)

    }

    fun beginPawnOnlyChess() {
        println("Pawns-Only Chess")

        println("First Player's name:")
        val namePlayer1 = readln()
        println("Second Player's name:")
        val namePlayer2 = readln()
        val player1 = Player(namePlayer1,'W')
        players.add(player1)
        val player2 = Player(namePlayer2,'B')
        players.add(player2)

        this.initializeBoardOnlyPawnChess()

        this.printBoard()

        while (true) {
            do {
                state = players[auth].takeTurn()
                if (state == State.WRONG_INPUT) println("Invalid Input")
            } while (state == State.WRONG_INPUT || state == State.NO_PIECE)


            if (state == State.EXIT) {
                println("Bye!")
                break
            }
            this.printBoard()

            // Check if won or draw
            state = players[auth].checkIfWonOrDraw()
            if (state == State.WHITE_WON) {
                println("White Wins!")
                println("Bye")
                break
            }
            if (state == State.BLACK_WON) {
                println("Black Wins!")
                println("Bye")
                break
            }
            if (state == State.DRAW) {
                println("Stalemate!")
                println("Bye")
                break
            }

            turnCount++
            auth = turnCount % 2
        }




    }


}

fun main() {
//    write your code here
    val game = ChessBoard()
    game.beginPawnOnlyChess()
}