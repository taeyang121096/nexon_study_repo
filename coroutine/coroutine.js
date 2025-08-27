// Promise 체인 (코루틴 없는 방식)
function task() {
  return new Promise(resolve => setTimeout(resolve, 1000));
}

function noCoroutineExample() {
  console.time("Promise Chain");
  let p = Promise.resolve();
  for (let i = 0; i < 3; i++) {
    p = p.then(() => task());
  }
  return p.then(() => console.timeEnd("Promise Chain"));
}

// async/await (코루틴처럼 작성)
async function coroutineExample() {
  console.time("Async Await");
  await Promise.all(Array.from({ length: 100000 }, () => task()));
  console.timeEnd("Async Await");
}

async function main() {
  await noCoroutineExample();
  await coroutineExample();
}

main();