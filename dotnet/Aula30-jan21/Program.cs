#define WITH_TCS
//#define OPER_THROW_EXCEPTION


// Experimente as diferentes possibilidades de descomentar as duas linhas acima e justifique 
// os diferentes comportamentos do programa

using System;
using static training2021i.question1_t1;
using System.Threading;
using System.Threading.Tasks;




namespace training2021i
{

	


	class Program
	{
		

		private static Task<int> OpAsync(int arg) {
			return Task.Run<int> (() => {
#if OPER_THROW_EXCEPTION
				throw new InvalidOperationException("Bad value!");
#else
				return arg +1;
#endif
			});
			
		}

		private static  Task<int> CtsBadExample() {
#if WITH_TCS
			TaskCompletionSource<int> tcs = new TaskCompletionSource<int>();
#else
			return 
#endif
			OpAsync(2)
			.ContinueWith(ant =>
			{
#if WITH_TCS
				tcs.SetResult(ant.Result);
#else
				return ant.Result;
#endif

			});
#if WITH_TCS
			return tcs.Task;
#endif
		}


		static void Main(string[] args) {
			
			CtsBadExample().ContinueWith(ant =>
			{
				Console.WriteLine(ant.Result);
				 
			})
			.Wait();

			 
			Console.WriteLine("Leave Main!");

		}
}
}
