export const StockService = {
    getStockList() {
        return [
            {
                id: 1,
                name: 'Apple',
                category: 'fruit',
                publisher: 'ABC농업',
                store: 'A점',
                size: '10EA',
                quantity: 100,
                safe: 10
            },
            {
                id: 2,
                name: 'Banana',
                category: 'fruit',
                publisher: 'XYZ농업',
                store: 'B점',
                size: '5EA',
                quantity: 50,
                safe: 5
            },
            {
                id: 3,
                name: 'Carrot',
                category: 'vegetable',
                publisher: '123농업',
                store: 'C점',
                size: '1EA',
                quantity: 200,
                safe: 20
            },
            {
                id: 4,
                name: 'Dairy Milk',
                category: 'dairy',
                publisher: 'DairyFarm',
                store: 'D점',
                size: '1L',
                quantity: 150,
                safe: 15
            },
            {
                id: 5,
                name: 'Eggs',
                category: 'poultry',
                publisher: 'EggFarm',
                store: 'E점',
                size: '12EA',
                quantity: 300,
                safe: 30
            },
            {
                id: 6,
                name: 'Fish',
                category: 'seafood',
                publisher: 'OceanCatch',
                store: 'F점',
                size: '500g',
                quantity: 80,
                safe: 8
            },
            {
                id: 7,
                name: 'Rice',
                category: 'grain',
                publisher: 'GrainFarm',
                store: 'G점',
                size: '5kg',
                quantity: 120,
                safe: 12
            },
            {
                id: 8,
                name: 'Wheat Flour',
                category: 'grain',
                publisher: 'FlourMill',
                store: 'H점',
                size: '2kg',
                quantity: 90,
                safe: 9
            },
            {
                id: 9,
                name: 'Olive Oil',
                category: 'oil',
                publisher: 'OliveFarm',
                store: 'I점',
                size: '500ml',
                quantity: 60,
                safe: 6
            },
            {
                id: 10,
                name: 'Honey',
                category: 'sweetener',
                publisher: 'BeeFarm',
                store: 'J점',
                size: '250g',
                quantity: 40,
                safe: 4
            }
        ];
    }
};
