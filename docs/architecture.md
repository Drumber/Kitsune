# Project Architecture & Structure

## Project Structure

The project consists of multiple layers. The layers are:

```
0. ui
1. domain
2. data
```

Dependencies between layers are unidirectional, meaning that a layer can only depend on the layers below it.

The dependencies are as follows: `ui -> domain -> data`

### Data Layer

The data layer contains the data models and data sources and is responsible for fetching and storing data.

The data layer is divided into the following packages/modules:

```
root
  - presentation
  - repository
  - mapper
source
  - local
  - JSON:API
  - GraphQL
```
