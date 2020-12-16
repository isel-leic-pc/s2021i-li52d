using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Aula22_async_methods.generators.async {
    using static AsyncOpers;

    public class AsyncGenerators {

        public static IEnumerable<Task<int>> TwoIncrementsAsync() {
            Task<int> task = RemoteIncrement(1);
            yield return task;

            yield return RemoteIncrement(task.Result);

        }



        public static IEnumerable<Task> ProcessAsReceivedAsync(
            List<Task<string>> tasks, Action<string> processor) {
            Task<Task<string>> task;
            while(tasks.Count > 0) {
                yield return task = Task.WhenAny(tasks);
                processor(task.Result.Result);
                tasks.Remove(task.Result);
            }

        }
    }
}
