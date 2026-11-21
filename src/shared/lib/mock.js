// Имитирует ответ сервера с задержкой
export const mockResponse = (data, ms = 600) =>
  new Promise((resolve) => setTimeout(() => resolve(data), ms));
