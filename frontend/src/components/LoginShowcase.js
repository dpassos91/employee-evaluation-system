import Logo_Branco from '../images/Logo_branco.png';

export default function LoginShowcase() {
  return (
<div className="relative w-full h-full bg-primary text-white px-6">
  
  {/* Conteúdo centrado manualmente */}
<div className="absolute top-1/2 left-1/2 transform -translate-x-[75%] -translate-y-[55%] z-10 text-center">
  <div className="relative inline-block">
    <img
      src={Logo_Branco}
      alt="SkillPath logo"
      className="w-[320px]"
    />
    <p className="text-3xl italic font-hand leading-none absolute left-1/2 -translate-x-1/2 top-[66%]">
      Learn. Evolve. Lead.
    </p>
  </div>
</div>

</div>

  );
}
