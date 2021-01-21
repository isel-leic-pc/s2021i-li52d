using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace training2021i
{
	class question2_t2
	{
        private R Oper<T,R>(T item) {
            return default(R);
		}

        private Task<R> OperAsync<T, R>(T item,  CancellationToken cToken) {
            return null;
        }

        private async Task<R> OperAsyncWithRetry<T, R>(T item, int maxRetries, CancellationToken cToken) {
            Exception last = null;
            while(maxRetries > 0) {
                try {
                    return await OperAsync<T, R>(item, cToken);
				}
                catch(TaskCanceledException ) {
                    throw;
				}
                catch(Exception e) {
                    maxRetries--;
                    last = e;
				}
            }
            throw last;
        }

        /*
         O método Oper é uma função sem efeitos colaterais e passível de múltiplas execuções em paralelo. 
         Esporadicamente pode lançar uma excepção (e.g. devido a erro de comunicação). 
         Realize uma versão assíncrona do método Compute seguindo o padrão TAP ( Task-based Asynchronous Pattern ) 
         usando a TPL e/ou os métodos assíncronos do C#. Assuma que tem disponível uma versão TAP do método Oper. 
         Tire partido do paralelismo potencial existente. A versão assíncrona deve tolerar erros na operação Oper , 
         tentando realizar de novo a operação para o mesmo parâmetro. 
         Contudo, não devem ser realizadas mais do que maxRetries retentativas para cada elemento de elems , 
         onde maxRetries é um parâmetro de entrada. 
         Caso seja excedido o número de tentativas, devem ser canceladas todas as operações pendentes.
        */
        public R[] Compute<T,R>(T[] elems) {
            var res = new R[elems.Length];
            for (int i = 0; i < elems.Length; i++) {
                res[i] = Oper<T,R>(elems[i]);
            }
            return res;
        }

        public async Task<R[]> ComputeAsync<T,R>(T[] elems, int maxRetries) {
            var cts = new CancellationTokenSource();
            var token = cts.Token;
            var res = new R[elems.Length];
            var tres = new Task<R>[elems.Length];
            for (int i = 0; i < elems.Length; i++) {
                tres[i] = OperAsyncWithRetry<T,R>(elems[i], maxRetries, token);
            }
            try {
                for (int i = 0; i < res.Length; i++) {
                    res[i] = await tres[i];
                }
            }
            catch(Exception) { 
                if (!cts.IsCancellationRequested) 
                    cts.Cancel();
                throw;
			}

            return res;
                    
        }
    }
}
