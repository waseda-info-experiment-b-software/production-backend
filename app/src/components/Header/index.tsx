import Image from "next/image";
import { FC } from "react";

export const Header: FC = () => {
  return (
    <header className="sticky top-0 w-full border-b border-solid border-b-gray-400">
      <nav className="flex items-center px-6 py-4">
        <div className="flex items-center gap-4">
          <Image src="/images/mogit-logo.png" alt="mogit" width={35} height={35} />
          <p>dashboard</p>
        </div>
      </nav>
    </header>
  )
}