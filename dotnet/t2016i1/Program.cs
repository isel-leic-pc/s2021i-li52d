using System;
using System.Collections.Generic;
using System.Threading.Tasks;

/**
A interface Services define um conjunto de operações síncronas que envolvem comunicação 
com um sistema externo com latência na ordem dos segundos, que usam os tipos A, B, C, e D 
previamente definidos.Todas estas operações não alteram estado (local ou do sistema externo),
contudo a sua execução pode resultar em excepção.O método estático Run executa uma sequência 
destas operações, produzindo um resultado final ou excepção.

A classe TAPExecute será a variante assíncrona de Execute, 
ao estilo Task based Asynchronous Pattern (TAP). 
Usando a funcionalidade oferecida pela Task Parallel Library (TPL) ou pelos
métodos async do C#, implemente o método RunAsync, que usa a interface TAPServices 
(variante TAP de Services que não tem de apresentar). 
A implementação deve exprimir o paralelismo potencial que existe no método Run. 
NOTA: na implementação não se admite a utilização de operações com bloqueios de controlo 
e pode ignorar a ocorrência de excepções.

**/
namespace t2016i1 {
    public class Program {
        static void Main(string[] args) {
            
        }
    }
}
