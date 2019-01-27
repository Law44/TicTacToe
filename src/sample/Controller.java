package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Duration;


import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    Button bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9;
    List<Button> buttonsPartida; //definimos todos los botones de juego y creamos una array donde los guardaremos

    @FXML Button play, reinicio; //boton de juego y reinicio

    @FXML TextArea firstPlayer, secondPlayer; //Campos donde tendremos nuestros nombres de jugador

    @FXML Text actualPlayer, turn; //Campos para ir cambiando dinamicamente el jugador



    String gamemode = "humanVShuman";
    String difficult = "easy";
    Map<String, Integer> ganadas = new HashMap<>();
    Map<String, Integer> empatadas = new HashMap<>();
    Map<String, Integer> perdidas = new HashMap<>();
    int casillas = 0;
    int turno = 1;
    int posicionGanadora = 0;

    boolean allowMove = false;
    boolean ganador = false;
    boolean VScpu = false;
    boolean noWinner = true;
    boolean buscarPosicion = true;
    int [] ganadora = null;
    boolean firstplay = true;
    boolean lastplay = false;//valores iniciales para que funcione la logica

    @FXML
    final private int[][] winningPositions = {
            {0, 4, 8},
            {2, 4, 6},
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8}
    }; // Array de arrays con las posiciones que equivalen a una posible posicion ganadora, de 0 a 8. Si una X o O ocupa las 3 posiciones
    // de alguna las posibiles, es una victoria.



    public Controller(){
    }

    @Override
    @FXML public void initialize(URL location, ResourceBundle resources) { // Al empezar la aplicacion, guardaremos los botones en una arraylist

        buttonsPartida = Arrays.asList(bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9);
        if (firstplay){
            reinicio.setDisable(true);
            firstplay = false;
        }
    }

    @FXML public void gamemode(ActionEvent event){ //Metodo para elegir el modo de juego, cada vez que cambie, resetearemos todos los datos
        // e impediremos los movimientos
        gamemode = "";
        play.setDisable(false);
        RadioButton b = (RadioButton) event.getSource();
        firstPlayer.setDisable(false);
        secondPlayer.setDisable(false);
        firstPlayer.setText("");
        secondPlayer.setText("");
        restart(event);
        allowMove = false;
        gamemode = b.getId();


    }

    @FXML public void closeGame(ActionEvent event){ //Metodo para cerrar el juego cuando se clique el boton aceptar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar");
        alert.setContentText("Seguro que quieres cerrar el juego?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            Platform.exit();
        }
    }

    @FXML public void setDifficulty(ActionEvent event){ //Identificar la dificultad cada vez que se cambie

        RadioMenuItem b = (RadioMenuItem) event.getSource();
        restart(event);
        difficult = b.getId();

    }

    @FXML public void partidas(ActionEvent event) throws IOException {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Resultados Globales");
        alert.getDialogPane().setMinWidth(500);

        alert.setTitle("Estadisticas");
        String total = "Jugador        Partidas Ganadas        Partidas Empatadas        Partidas Perdidas\n";
        Map<String, Integer> sortedMap = sortByValue(ganadas);
        for(String key: sortedMap.keySet()) {
            total += key + "                           " + sortedMap.get(key) + "                           " + empatadas.get(key) + "                                " + perdidas.get(key) +"\n";
        }

        alert.setContentText(total);
        alert.show();

    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @FXML public void gameplay(ActionEvent event){ //jugabilidad
        Button b = (Button) event.getSource();
        reinicio.setDisable(false);

        if (gamemode.equals("humanVShuman")){ //si se selecciona el modo persona vs persona
            firstPlayer.setDisable(true);
            secondPlayer.setDisable(true); //bloqueamos el cambio de nombre
            if (turno == 1){
                actualPlayer.setText(firstPlayer.getText());
                turno = 2;
            }
            else {
                actualPlayer.setText(secondPlayer.getText());
                turno = 1;
            } //control del turno para ir alternando a cada nueva partida
            b.setDisable(true);
            allowMove = true; //permitimos el juego
            VScpu = false; // y bloqueamos la CPU
        }
        else if (gamemode.equals("cpuVScpu")){ //si se seleccion CPU vs CPU
            firstPlayer.setText("CPU");
            firstPlayer.setDisable(true);
            secondPlayer.setText("CPU");
            secondPlayer.setDisable(true); //ponemos ambos nombres de jugador a CPU y bloqueamos el cambio de nombre
            VScpu = false;
            allowMove = false; //bloqueamos el movimiento
            while (!ganador) { //bucle en el que se llamara constantemente al metodo que hace el movimiento de la CPU
                jugadaCpuEasy();
                if (casillas >= 5) {
                    this.checkVictory();
                }
            }
        }
        else if (gamemode.equals("humanVScpu")){ //Si se elige humano contra CPU
            if (difficult.equals("hard") && buscarPosicion){ // si la dificultad es dificil
                jugadaCpuHard(); //la cpu ira primero
            }
            else if  (difficult.equals("normal") && buscarPosicion){
                firstPlayer.setDisable(true);
                secondPlayer.setText("CPU");
                secondPlayer.setDisable(true);
            }
            else if  (difficult.equals("easy") && buscarPosicion){
                firstPlayer.setDisable(true);
                secondPlayer.setText("CPU");
                secondPlayer.setDisable(true);
            } //En caso contrario, simplemente asignamos el nombre CPU al enemigo
            if (turno == 1){
                actualPlayer.setText(firstPlayer.getText());
                turno = 2;
            }
            else {
                actualPlayer.setText(secondPlayer.getText());
                turno = 1;
            } //Control para ir cambiando de turno
            b.setDisable(true);
            allowMove = true;
            VScpu = true; //permitimos movimiento y confirmamos que es contra la CPU
        }
        if (!ganadas.containsKey(firstPlayer.getText())){
            ganadas.put(firstPlayer.getText(), 0);
            empatadas.put(firstPlayer.getText(), 0);
            perdidas.put(firstPlayer.getText(), 0);

        }
        if (!ganadas.containsKey(secondPlayer.getText())){
            ganadas.put(secondPlayer.getText(),0);
            empatadas.put(secondPlayer.getText(), 0);
            perdidas.put(secondPlayer.getText(), 0);
        }
    }


    @FXML public void movimiento(ActionEvent event){ //metodo para la jugada
        if (allowMove) { //si podemos jugar
            allowMove = false; //lo cambiamos a false para controlar el movimiento en el turno enemigo
            Button b = (Button) event.getSource();

            if (b.getText().equals("")) {
                //vamos cambiando el nombre del turno
                if (lastplay) {
                    b.setText("O");
                    casillas++;
                } else {
                    b.setText("X");
                    casillas++;
                } //marcamos la casilla segun que jugador toque

                lastplay = !lastplay; //e cambiamos el turno
                if (turno == 1){
                    actualPlayer.setText(firstPlayer.getText());
                    turno = 2;
                }
                else {
                    actualPlayer.setText(secondPlayer.getText());
                    turno = 1;
                }
            }
            if (casillas >= 5) { //si se han marcado 5 casillas o mas, comprobamos si alguien ha ganado. Es imposible que sea antes
                this.checkVictory();
            }
            if(!VScpu){ //si no estamos jugando contra la CPU, permitimos volver a mover
                allowMove = true;
            }

            if (VScpu && !ganador){ //si estamos jugando contra la CPU
                if (difficult.equals("easy")) { //comprobamos la dificultad
                    actualPlayer.setText(secondPlayer.getText());
                    Timeline twoSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent event) {
                            jugadaCpuEasy();
                            checkVictory();
                            allowMove = true;
                            actualPlayer.setText(firstPlayer.getText());
                            turno = 2;

                        }
                    })); //delay para que tarde un poco en jugar y llamamos el metodo correspondiente.
                    twoSecondsWonder.play();

                } //hacemos lo mismo en las otras dificultades
                else if (difficult.equals("normal")) {
                    actualPlayer.setText(secondPlayer.getText());
                    Timeline twoSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent event) {

                            jugadaCpuNormal();
                            checkVictory();
                            allowMove = true;
                            actualPlayer.setText(firstPlayer.getText());
                            turno = 2;

                        }
                    }));
                    twoSecondsWonder.play();

                }

                else if (difficult.equals("hard")) {
                    actualPlayer.setText(firstPlayer.getText());
                    Timeline twoSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent event) {

                            jugadaCpuHard();
                            checkVictory();
                            allowMove = true;
                            actualPlayer.setText(secondPlayer.getText());
                            turno = 1;
                        }
                    }));
                    twoSecondsWonder.play();

                }
            }

        }
    }

    private  void jugadaCpuHard(){ //metodo de la dificultad dificil. NO SE LE PUEDE GANAR
        firstPlayer.setDisable(true);
        String temp = firstPlayer.getText();
        firstPlayer.setText("CPU");
        secondPlayer.setText(temp);
        secondPlayer.setDisable(true);
        Random random = new Random();
        if (buscarPosicion){ //La primera vez que se ejecute, seleccionara aleatoriamente una de las posiciones ganadoras
            ganadora = winningPositions[random.nextInt(winningPositions.length)];
            buscarPosicion = false;
        }
            if (!ganador) {
                    if (lastplay) { //sin importar que hayas marcado esa casilla, la CPU la sobrescribira
                        buttonsPartida.get(ganadora[posicionGanadora]).setText("O");
                        casillas++;
                    } else {
                        buttonsPartida.get(ganadora[posicionGanadora]).setText("X");
                        casillas++;
                    }
                    lastplay = !lastplay;

                posicionGanadora++;
            }


    }

    private void jugadaCpuNormal() { //Metodo de dificultad normal

        for (int[] filled : this.winningPositions) { //cada vez que juegue, mirara todas las posiciones

            int victoryX = 0;
            int victoryO = 0;
            int vacia = 999;

            for (int b : filled){

                if (buttonsPartida.get(b).getText().equals("")){
                    vacia = b;
                    continue;
                }else if (!lastplay){
                    if (buttonsPartida.get(b).getText().equals("O")) {
                        victoryO++;
                    }
                }else {
                    if (buttonsPartida.get(b).getText().equals("X")){
                        victoryX++;
                    }
                }

            }

            if (victoryO==2&& vacia!=999){ //si el jugador fuera a ganar, lo bloqueara
                buttonsPartida.get(vacia).setText("X");
                actualPlayer.setText(secondPlayer.getText());
                lastplay=!lastplay;

                casillas++;
                return;
            }else if (victoryX==2&& vacia!=999){
                buttonsPartida.get(vacia).setText("O");
                actualPlayer.setText(firstPlayer.getText());
                lastplay=!lastplay;

                casillas++;
                return;
            }
        }
        jugadaCpuEasy();
    }


    public void jugadaCpuEasy(){ //metodo facil
        Button b ;
        Random random = new Random();

        do {

            b = buttonsPartida.get(random.nextInt( 9)); //marcara una casilla aleatoriamente
        }while (!b.getText().equals(""));
        if(!ganador){
            if (b.getText().equals("")) {
                if (lastplay) {
                    b.setText("O");
                    casillas++;
                } else {
                    b.setText("X");
                    casillas++;
                }
                lastplay = !lastplay;
            }

        }
    }

    @FXML public void restart(ActionEvent event){ //metodo para reiniciar todo a base

        buttonsPartida.forEach(b -> b.setText(""));
        ganador = false;
        noWinner = true;
        casillas = 0;
        allowMove = true;
        turn.setText("Turn of: ");
        posicionGanadora = 0;
        buscarPosicion = true;
        lastplay = false;
        turno = 1;
        if (gamemode.equals("humanVShuman")) {
            String temporal = firstPlayer.getText();
            firstPlayer.setText(secondPlayer.getText());
            secondPlayer.setText(temporal);
            actualPlayer.setText(firstPlayer.getText());
            turno = 2;
        } else if (difficult.equals("hard")){
            jugadaCpuHard();
        }

    }



    public void checkVictory() { //metodo para comprobar ganador

        for (int[] filled : this.winningPositions) { //recorreremos las arrays que hay en la array de posiciones ganadoras

            int victoryX = 0;
            int victoryO = 0;

            for (int b: filled){

                if (buttonsPartida.get(b).getText().equals("")){
                    continue;
                }
                else if (buttonsPartida.get(b).getText().equals("X")){
                    victoryX++;
                }
                else if (buttonsPartida.get(b).getText().equals("O")){
                    victoryO++;
                } //cada vez que encuentre una O o X sumara +1 a la variable control correspondiente

            }

            if (victoryO == 3){
                ganador = true;
                allowMove = false;
                noWinner = false;
                turn.setText("WINNER: ");
                actualPlayer.setText(secondPlayer.getText());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText(secondPlayer.getText() + " WINS");
                alert.show();

                ganadas.put(secondPlayer.getText(), ganadas.get(secondPlayer.getText())+1);
                perdidas.put(firstPlayer.getText(), perdidas.get(firstPlayer.getText())+1);
                break;
            }
            else if (victoryX == 3){
                ganador = true;
                allowMove = false;
                noWinner = false;

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Look, an Information Dialog");

                alert.setContentText(firstPlayer.getText() + " WINS");
                turn.setText("WINNER: ");
                actualPlayer.setText(firstPlayer.getText());

                alert.show();

                ganadas.put(firstPlayer.getText(), ganadas.get(firstPlayer.getText())+1);
                perdidas.put(secondPlayer.getText(), perdidas.get(secondPlayer.getText())+1);


                break;
            } //si alguna de las 2 variables es 3, ese jugador ha ganado y ponemos la informacion correspondiente
        }

        if (casillas == 9 && noWinner){ //si hemos llegado a 9 casillas marcadas y no hay ganador, avisamos del empate
            ganador = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Look, an Information Dialog");
            alert.setContentText("TIE!!!");

            alert.show();

            empatadas.put(firstPlayer.getText(), empatadas.get(firstPlayer.getText())+1);
            empatadas.put(secondPlayer.getText(), empatadas.get(secondPlayer.getText())+1);
        }
    }
}
