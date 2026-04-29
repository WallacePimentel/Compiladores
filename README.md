# Compiladores

## Como executar

### Via terminal (Windows: PowerShell / cmd)

Pré-requisito: ter um JDK instalado (com `javac` e `java` no PATH).

1) Entre na pasta do módulo (a que contém `src/`):

```bash
cd .\Compiladores
```

2) Compile:

```bash
javac -encoding UTF-8 -d out -sourcepath src src\Main.java
```

3) Execute:

```bash
java -cp out Main
```

### Via terminal (Linux/macOS: bash)

Pré-requisito: ter um JDK instalado (com `javac` e `java` no PATH).

1) Entre na pasta do módulo (a que contém `src/`):

```bash
cd ./Compiladores
```

2) Compile:

```bash
javac -encoding UTF-8 -d out -sourcepath src src/Main.java
```

3) Execute:

```bash
java -cp out Main
```

### Via IntelliJ

- Abra o projeto e execute a classe `Main`.
- Se aparecer erro de arquivo não encontrado, ajuste o *Working directory* da configuração de execução para a pasta `Compiladores/` (a que contém `src/exemplos`).

## Como escolher os testes

Ao rodar, o programa pede duas entradas no console:

1) **Tipo de teste**: digite `erro` ou `sucesso` (atalhos aceitos: `e`/`s`).
2) **Número do teste**: digite `1`, `2` ou `3`.

Com isso, ele lê o arquivo:

- `src/exemplos/teste_<tipo><n>.txt`
	- Exemplos: `src/exemplos/teste_erro1.txt`, `src/exemplos/teste_sucesso2.txt`.

## Saídas geradas

Depois de executar, as saídas são gravadas em:

- `src/exemplos/saida_scanner.txt` (tokens do scanner)
- `src/exemplos/saida_parser.txt` (resultado do parser/Ast ou lista de erros)
- `src/exemplos/saida_usuario.txt` (resultado do parser/Ast ou lista de erros melhorada para o programador)
