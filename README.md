<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" alt="Ícone do SplitUp" width="120" />
</p>

# SplitUp

Aplicativo Android para dividir comandas entre amigos de forma simples, rápida e offline.  
Com o SplitUp você cria comandas, adiciona itens, escolhe quem participa de cada item e vê automaticamente quanto cada pessoa deve pagar.

## O Que O App Faz

- Cria e gerencia comandas abertas.
- Adiciona/remover participantes por comanda.
- Adiciona itens com quantidade, valor e divisão por pessoas específicas.
- Mostra totais por item e por pessoa.
- Fecha comandas e move para histórico.
- Permite editar/excluir dados com validações de regras de negócio.
- Exporta resumo da comanda em PDF.

## Especificações Técnicas

- Plataforma: `Android`
- Linguagem: `Kotlin`
- UI: `Jetpack Compose` + `Material 3`
- Arquitetura: `MVVM` + camadas `ui/domain/data`
- Injeção de dependência: `Hilt`
- Persistência local: `Room`
- Navegação: `Navigation Compose`
- Processamento de anotações: `KSP`
- Logs: `Timber`
- Execução: `100% offline` (sem backend obrigatório)

## Requisitos Do Projeto

- `minSdk`: 32
- `targetSdk`: 35
- `compileSdk`: 35
- Java: 11
- Kotlin JVM target: 11

## Licença

Este projeto está sob a licença definida em `LICENSE`.
