import os
import pandas as pd

DATA_FOLDER = 'data-rdiff'
OUTPUT_FOLDER = 'output-data-rdiff'
ARQUIVO_FINAL = 'total_refatoracoes_data-rdiff.csv'


def remove_linhas_repetidas_e_calcula_incidencia(file_path, output_folder):
    try:
        df = pd.read_csv(file_path, encoding="ISO-8859-1")
    except UnicodeDecodeError:
        print(f"Erro de codificação ao ler o arquivo: {file_path}")
        return
     
    df = df.drop(columns=['Descrição'])
    df = df.drop(columns=['Definição'])
    
    df['Incidência'] = df.groupby(['Hash', 'Refatoração'])[
        'Hash'].transform('count')
    
    df.drop_duplicates(subset=['Hash', 'Refatoração'],
                       keep='first', inplace=True)

    # Obter o nome do arquivo de entrada (sem a extensão)
    base_filename = os.path.splitext(os.path.basename(file_path))[0]

    output_file = os.path.join(
        output_folder, f'incidencia_{base_filename}.csv')
    df.to_csv(output_file, index=False)


def calcular_incidencia_refatoracao(file_path, output_folder):
    try:
        df = pd.read_csv(file_path, encoding="ISO-8859-1")
    except UnicodeDecodeError:
        print(f"Erro de codificação ao ler o arquivo: {file_path}")
        return

 
    df = df.drop(columns=['Descrição'])
    df = df.drop(columns=['Definição'])
    
    # Calcula a incidência de cada refatoração
    incidencia = df['Refatoração'].value_counts().reset_index()
    incidencia.columns = ['Refatoração', 'Incidência']

    # Define o caminho do arquivo de saída
    base_filename = os.path.splitext(os.path.basename(file_path))[0]
    output_file = os.path.join(
        output_folder, f'incidencia_{base_filename}.csv')

    # Salva o resultado em um novo arquivo CSV
    incidencia.to_csv(output_file, index=False)


def processar_arquivos_csv_e_removente_linhas_repetidas():

    output_folder = OUTPUT_FOLDER + '/sem-linhas-repetidas'
    os.makedirs(output_folder, exist_ok=True)

    # 1 - Percorrer os arquivos CSV, remover linhas repetidas e criar tabela com 'Hash', 'Refatoração' e 'Incidência'
    for root, dirs, files in os.walk(DATA_FOLDER):
        for file in files:
            if file.endswith('.csv'):
                file_path = os.path.join(root, file)
                file_output_folder = os.path.join(
                    output_folder, os.path.basename(root))
                os.makedirs(file_output_folder, exist_ok=True)

                remove_linhas_repetidas_e_calcula_incidencia(
                    file_path, file_output_folder)


def processar_arquivos_csv_e_calcular_incidencia_refatoracao():

    output_folder = OUTPUT_FOLDER + '/incidencia-refatoracao'
    os.makedirs(output_folder, exist_ok=True)

    # 1 - Percorrer os arquivos CSV, remover linhas repetidas e criar tabela com 'Hash', 'Refatoração' e 'Incidência'
    for root, dirs, files in os.walk(DATA_FOLDER):
        for file in files:
            if file.endswith('.csv'):
                file_path = os.path.join(root, file)
                file_output_folder = os.path.join(
                    output_folder, os.path.basename(root))
                os.makedirs(file_output_folder, exist_ok=True)

                calcular_incidencia_refatoracao(
                    file_path, file_output_folder)


def processar_pasta_entrada(input_folder, output_folder):
    refatoracoes = {}  # Dicionário para rastrear a contagem de refatorações

    # Percorre todos os arquivos CSV na pasta de entrada
    for root, dirs, files in os.walk(input_folder):
        for file in files:
            if file.endswith('.csv'):
                file_path = os.path.join(root, file)

                try:
                    df = pd.read_csv(file_path, encoding='ISO-8859-1')
                except UnicodeDecodeError:
                    print(f"Erro de codificação ao ler o arquivo: {file_path}")
                    continue

                # Contagem de refatorações neste arquivo
                refatoracoes_arquivo = df['Refatoração'].value_counts(
                ).to_dict()
                # Atualiza o dicionário de refatorações com a contagem deste arquivo
                for refatoracao, quantidade in refatoracoes_arquivo.items():
                    if refatoracao in refatoracoes:
                        refatoracoes[refatoracao] += quantidade
                    else:
                        refatoracoes[refatoracao] = quantidade

    # Cria um DataFrame com a contagem total de refatorações
    df_total = pd.DataFrame(list(refatoracoes.items()), columns=[
                            'Refatoração', 'Quantidade'])
    df_total = df_total.sort_values(by='Quantidade', ascending=False)

    # Define o caminho do arquivo de saída
    output_file = os.path.join(output_folder, ARQUIVO_FINAL)

    # Salva o resultado em um novo arquivo CSV
    df_total.to_csv(output_file, index=False)


processar_arquivos_csv_e_removente_linhas_repetidas()
processar_arquivos_csv_e_calcular_incidencia_refatoracao()
processar_pasta_entrada(DATA_FOLDER, OUTPUT_FOLDER)
