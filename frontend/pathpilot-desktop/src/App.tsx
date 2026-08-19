import { useEffect, useState } from "react";
import type { PendingMoveType } from "./types/PendingMoveType";
import PendingMove from "./components/PendingMove";
import { approvePendingMove, deletePendingMove, getPendingMove, getPendingMoves } from "./api/api";
import { getCurrentWindow, LogicalSize } from "@tauri-apps/api/window";

function App() {
    const [pendingMoves, setPendingMoves] = useState<PendingMoveType[]>([]);

    useEffect(() => {
        const resizeWindow = async () => {
            const visibleMoves = Math.min(pendingMoves.length, 5);
            const baseHeight = 80;
            const rowHeight = 52;
            const height = baseHeight + (visibleMoves * rowHeight);

            await getCurrentWindow().setSize(new LogicalSize(340, height));
        };

        resizeWindow();
    }, [pendingMoves.length]);

    useEffect(() => {
        const loadPendingMoves = async () => {
            const moves = await getPendingMoves();
            setPendingMoves(moves);
        };

        loadPendingMoves();
    }, []);

    useEffect(() => {
        const eventSource = new EventSource("http://localhost:8080/api/events/pending-moves");

        eventSource.addEventListener("pending-move-created", async (event) => {
            const id = Number(event.data);
            const move = await getPendingMove(id);
            setPendingMoves(current => [...current, move]);
        });

        eventSource.addEventListener("pending-move-deleted", (event) => {
            const id = Number(event.data);
            setPendingMoves(current => current.filter(move => move.id !== id));
        });

        return () => eventSource.close();
    }, []);

    const handleApprove = async (id: number) => {
        await approvePendingMove(id);
    };

    const handleDelete = async (id: number) => {
        await deletePendingMove(id);
    };

    return (
        <div className="h-full w-full overflow-hidden bg-transparent p-5 text-white select-none antialiased">
            {pendingMoves.length === 0
                ? <p className="text-sm text-white/50">Downloaded files will appear here</p>
                : pendingMoves.slice(0, 5).map(move => (
                    <PendingMove
                        key={move.id}
                        {...move}
                        onApprove={handleApprove}
                        onDelete={handleDelete}
                    />
                ))}
        </div>
    );
}

export default App;
