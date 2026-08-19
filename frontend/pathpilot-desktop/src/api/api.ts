import type { PendingMoveType } from "../types/PendingMoveType";

const BASE_URL = "http://localhost:8080/api/pending-moves";

type PendingMoveResponse = Omit<PendingMoveType, "operationType"> & {
    operationType?: PendingMoveType["operationType"];
    operation_type?: PendingMoveType["operationType"];
};

function normalizePendingMove(move: PendingMoveResponse): PendingMoveType {
    return {
        ...move,
        operationType: move.operationType ?? move.operation_type ?? "MOVE",
    };
}

export async function getPendingMoves(): Promise<PendingMoveType[]> {
    const response = await fetch(BASE_URL);
    const moves: PendingMoveResponse[] = await response.json();
    return moves.map(normalizePendingMove);
}

export async function getPendingMove(id: number): Promise<PendingMoveType> {
    const response = await fetch(`${BASE_URL}/${id}`);
    const move: PendingMoveResponse = await response.json();
    return normalizePendingMove(move);
}

export async function approvePendingMove(id: number): Promise<void> {
    await fetch(`${BASE_URL}/${id}/approve`, {
        method: "POST",
    });
}

export async function deletePendingMove(id: number): Promise<void> {
    await fetch(`${BASE_URL}/${id}`, {
        method: "DELETE",
    });
}
