import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

enum CategoriaEvento {
    FESTA, SHOW, ESPORTE, CONFERENCIA, TEATRO, OUTROS;

    public static void listarCategorias() {
        for (CategoriaEvento c : CategoriaEvento.values()) {
            System.out.println(c.ordinal() + " - " + c.name());
        }
    }

    public static CategoriaEvento fromInt(int i) {
        return CategoriaEvento.values()[i];
    }
}

class Usuario {
    private String nome;
    private String email;
    private String telefone;

    public Usuario(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    @Override
    public String toString() {
        return nome + " (" + email + ", " + telefone + ")";
    }

    // Pra salvar no arquivo (se quiser)
    public String toDataString() {
        return nome + ";" + email + ";" + telefone;
    }

    public static Usuario fromDataString(String data) {
        String[] parts = data.split(";");
        if(parts.length != 3) return null;
        return new Usuario(parts[0], parts[1], parts[2]);
    }
}

class Evento {
    private String nome;
    private String endereco;
    private CategoriaEvento categoria;
    private LocalDateTime horario;
    private String descricao;
    private List<Usuario> participantes;

    public Evento(String nome, String endereco, CategoriaEvento categoria, LocalDateTime horario, String descricao) {
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.horario = horario;
        this.descricao = descricao;
        this.participantes = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public CategoriaEvento getCategoria() { return categoria; }
    public LocalDateTime getHorario() { return horario; }
    public String getDescricao() { return descricao; }
    public List<Usuario> getParticipantes() { return participantes; }

    public void adicionarParticipante(Usuario u) {
        if(!participantes.contains(u)) {
            participantes.add(u);
        }
    }

    public void removerParticipante(Usuario u) {
        participantes.remove(u);
    }

    public boolean isParticipando(Usuario u) {
        return participantes.contains(u);
    }

    public boolean estaOcorreAgora() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(horario) && now.isBefore(horario.plusHours(4)); // evento dura 4h por exemplo
    }

    public boolean jaOcorreu() {
        return LocalDateTime.now().isAfter(horario.plusHours(4));
    }

    @Override
    public String toString() {
        String status = "";
        if(estaOcorreAgora()) status = "[OCORRENDO AGORA]";
        else if(jaOcorreu()) status = "[JÁ OCORREU]";
        else status = "[AGENDADO]";
        return nome + " - " + categoria + " - " + endereco + " - " + horario.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " " + status;
    }

    public String toDataString() {
        // salvar: nome;endereco;categoria;horario;descricao;participantesEmails separados por ','
        StringBuilder sb = new StringBuilder();
        sb.append(nome).append(";")
          .append(endereco).append(";")
          .append(categoria.name()).append(";")
          .append(horario.toString()).append(";")
          .append(descricao.replace(";", ",")).append(";");

        // salvar só emails dos participantes pra linkar no carregamento
        for(int i = 0; i < participantes.size(); i++) {
            sb.append(participantes.get(i).getEmail());
            if(i < participantes.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    public static Evento fromDataString(String data, List<Usuario> usuarios) {
        // data format: nome;endereco;categoria;horario;descricao;email1,email2
        String[] parts = data.split(";", 6);
        if(parts.length < 5) return null;

        String nome = parts[0];
        String endereco = parts[1];
        CategoriaEvento categoria = CategoriaEvento.valueOf(parts[2]);
        LocalDateTime horario = LocalDateTime.parse(parts[3]);
        String descricao = parts[4];
        Evento evento = new Evento(nome, endereco, categoria, horario, descricao);

        if(parts.length == 6) {
            String[] emails = parts[5].split(",");
            for(String email : emails) {
                for(Usuario u : usuarios) {
                    if(u.getEmail().equals(email)) {
                        evento.adicionarParticipante(u);
                        break;
                    }
                }
            }
        }
        return evento;
    }
}

public class SistemaEventos {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Evento> eventos = new ArrayList<>();
    private Usuario usuarioAtual = null;
    private Scanner scanner = new Scanner(System.in);
    private static final String ARQUIVO = "events.data";

    public void iniciar() {
        carregarDados();
        System.out.println("Bem-vinda ao sistema de eventos da sua cidade, Thay!");
        menuPrincipal();
        salvarDados();
    }

    private void menuPrincipal() {
        while(true) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            if(usuarioAtual == null) {
                System.out.println("1 - Cadastrar Usuário");
                System.out.println("2 - Login");
                System.out.println("0 - Sair");
                System.out.print("Escolha: ");
                int op = Integer.parseInt(scanner.nextLine());
                switch(op) {
                    case 1: cadastrarUsuario(); break;
                    case 2: login(); break;
                    case 0: return;
                    default: System.out.println("Opção inválida.");
                }
            } else {
                System.out.println("Usuário: " + usuarioAtual.getNome());
                System.out.println("1 - Cadastrar Evento");
                System.out.println("2 - Listar Eventos");
                System.out.println("3 - Participar de Evento");
                System.out.println("4 - Cancelar Participação");
                System.out.println("5 - Meus Eventos Confirmados");
                System.out.println("6 - Logout");
                System.out.println("0 - Sair");
                System.out.print("Escolha: ");
                int op = Integer.parseInt(scanner.nextLine());
                switch(op) {
                    case 1: cadastrarEvento(); break;
                    case 2: listarEventos(); break;
                    case 3: participarEvento(); break;
                    case 4: cancelarParticipacao(); break;
                    case 5: listarEventosConfirmados(); break;
                    case 6: usuarioAtual = null; break;
                    case 0: return;
                    default: System.out.println("Opção inválida.");
                }
            }
        }
    }

    private void cadastrarUsuario() {
        System.out.println("\n--- Cadastrar Usuário ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        if(buscarUsuarioPorEmail(email) != null) {
            System.out.println("Email já cadastrado.");
            return;
        }
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();
        Usuario u = new Usuario(nome, email, telefone);
        usuarios.add(u);
        System.out.println("Usuário cadastrado com sucesso!");
    }

    private void login() {
        System.out.print("Digite seu email para login: ");
        String email = scanner.nextLine();
        Usuario u = buscarUsuarioPorEmail(email);
        if(u == null) {
            System.out.println("Usuário não encontrado. Cadastre-se antes.");
        } else {
            usuarioAtual = u;
            System.out.println("Login efetuado! Bem-vinda, " + usuarioAtual.getNome());
        }
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        for(Usuario u : usuarios) {
            if(u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    private void cadastrarEvento() {
        System.out.println("\n--- Cadastrar Evento ---");
        System.out.print("Nome do Evento: ");
        String nome = scanner.nextLine();
        System.out.print("Endereço: ");
        String endereco = scanner.nextLine();

        System.out.println("Categorias:");
        CategoriaEvento.listarCategorias();
        System.out.print("Escolha a categoria (número): ");
        int cat = Integer.parseInt(scanner.nextLine());
        CategoriaEvento categoria = CategoriaEvento.fromInt(cat);

        System.out.print("Data e Hora (formato: yyyy-MM-dd HH:mm): ");
        String dtStr = scanner.nextLine();
        LocalDateTime horario;
        try {
            horario = LocalDateTime.parse(dtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch(Exception e) {
            System.out.println("Formato de data/hora inválido.");
            return;
        }

        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();

        Evento ev = new Evento(nome, endereco, categoria, horario, descricao);
        eventos.add(ev);
        System.out.println("Evento cadastrado com sucesso!");
    }

    private void listarEventos() {
        if(eventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
            return;
        }
        eventos.sort(Comparator.comparing(Evento::getHorario));
        System.out.println("\n--- Eventos cadastrados ---");
        for(int i = 0; i < eventos.size(); i++) {
            System.out.println(i + " - " + eventos.get(i));
        }
    }

    private void participarEvento() {
        listarEventos();
        if(eventos.isEmpty()) return;
        System.out.print("Digite o número do evento para participar: ");
        int idx = Integer.parseInt(scanner.nextLine());
        if(idx < 0 || idx >= eventos.size()) {
            System.out.println("Evento inválido.");
            return;
        }
        Evento ev = eventos.get(idx);
        if(ev.isParticipando(usuarioAtual)) {
            System.out.println("Você já está participando deste evento.");
        } else {
            ev.adicionarParticipante(usuarioAtual);
            System.out.println("Participação confirmada no evento: " + ev.getNome());
        }
    }

    private void cancelarParticipacao() {
        List<Evento> meusEventos = new ArrayList<>();
        for(Evento e : eventos) {
            if(e.isParticipando(usuarioAtual)) {
                meusEventos.add(e);
            }
        }
        if(meusEventos.isEmpty()) {
            System.out.println("Você não está participando de nenhum evento.");
            return;
        }
        System.out.println("--- Seus Eventos Confirmados ---");
        for(int i = 0; i < meusEventos.size(); i++) {
            System.out.println(i + " - " + meusEventos.get(i));
        }
        System.out.print("Digite o número do evento para cancelar participação: ");
        int idx = Integer.parseInt(scanner.nextLine());
        if(idx < 0 || idx >= meusEventos.size()) {
            System.out.println("Evento inválido.");
            return;
        }
        Evento ev = meusEventos.get(idx);
        ev.removerParticipante(usuarioAtual);
        System.out.println("Participação cancelada no evento: " + ev.getNome());
    }

    private void listarEventosConfirmados() {
        System.out.println("\n--- Eventos em que você está participando ---");
        boolean tem = false;
        for(Evento e : eventos) {
            if(e.isParticipando(usuarioAtual)) {
                System.out.println(e);
                tem = true;
            }
        }
        if(!tem) {
            System.out.println("Nenhum evento confirmado.");
        }
    }

    private void salvarDados() {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO))) {
            // salvar usuários
            bw.write("#USUARIOS\n");
            for(Usuario u : usuarios) {
                bw.write(u.toDataString() + "\n");
            }
            bw.write("#EVENTOS\n");
            for(Evento e : eventos) {
                bw.write(e.toDataString() + "\n");
            }
            System.out.println("Dados salvos no arquivo " + ARQUIVO);
        } catch(IOException e) {
            System.out.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    private void carregarDados() {
        Path path = Paths.get(ARQUIVO);
        if(!Files.exists(path)) return;

        try(BufferedReader br = new BufferedReader(new FileReader(ARQUIVO))) {
            String linha;
            boolean lendoUsuarios = false;
            boolean lendoEventos = false;
            List<String> linhasEventos = new ArrayList<>();

            while((linha = br.readLine()) != null) {
                if(linha.equals("#USUARIOS")) {
                    lendoUsuarios = true;
                    lendoEventos = false;
                    continue;
                }
                if(linha.equals("#EVENTOS")) {
                    lendoEventos = true;
                    lendoUsuarios = false;
                    continue;
                }
                if(lendoUsuarios) {
                    Usuario u = Usuario.fromDataString(linha);
                    if(u != null) usuarios.add(u);
                } else if(lendoEventos) {
                    linhasEventos.add(linha);
                }
            }
            // Agora criar eventos após carregar usuários (pois precisa dos usuários para linkar participantes)
            for(String eStr : linhasEventos) {
                Evento e = Evento.fromDataString(eStr, usuarios);
                if(e != null) eventos.add(e);
            }
            System.out.println("Dados carregados do arquivo " + ARQUIVO);
        } catch(IOException e) {
            System.out.println("Erro ao carregar dados: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new SistemaEventos().iniciar();
    }
}
