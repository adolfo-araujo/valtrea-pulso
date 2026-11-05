import { Link } from 'react-router-dom'
import { LEGAL } from '../lib/legal'
import { Pagina, estilos } from '../components/ui'

const Email = () => <a href={`mailto:${LEGAL.email}`} className={estilos.link}>{LEGAL.email}</a>

function Documento({ titulo, children }) {
  return (
    <Pagina titulo={titulo} largura="max-w-3xl">
      <p className="mb-6 text-sm text-muted">Atualizado em {LEGAL.updatedAt}.</p>
      <div className="space-y-4 leading-relaxed [&_h2]:mt-8 [&_h2]:text-xl [&_h2]:font-bold [&_li]:ml-5 [&_li]:list-disc [&_td]:border [&_td]:border-border [&_td]:p-2 [&_th]:border [&_th]:border-border [&_th]:bg-surface-2 [&_th]:p-2 [&_th]:text-left">
        {children}
      </div>
    </Pagina>
  )
}

function Tabela({ cabecalho, linhas }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-sm">
        <thead><tr>{cabecalho.map((c) => <th key={c} scope="col">{c}</th>)}</tr></thead>
        <tbody>{linhas.map((l) => <tr key={l[0]}>{l.map((c, i) => <td key={i}>{c}</td>)}</tr>)}</tbody>
      </table>
    </div>
  )
}

export function PrivacidadePage() {
  return (
    <Documento titulo="Política de privacidade">
      <p>
        Esta Política explica como o {LEGAL.appName} (site {LEGAL.site} e aplicativo Android) trata dados pessoais,
        conforme a Lei Geral de Proteção de Dados Pessoais (Lei nº 13.709/2018, "LGPD").
      </p>

      <h2>1. Quem é o responsável</h2>
      <p>
        O controlador é {LEGAL.controller} ({LEGAL.controllerType}), {LEGAL.forum}. Canal para qualquer assunto sobre os
        seus dados: <Email />. Como agente de tratamento de pequeno porte, não indicamos encarregado (Resolução CD/ANPD nº
        2/2022); o canal é respondido diretamente pelo responsável.
      </p>

      <h2>2. Dados que tratamos, para quê e com qual base legal</h2>
      <Tabela cabecalho={['Dado', 'Para quê', 'Base legal (LGPD)']} linhas={[
        ['Seu nome, e-mail e senha (guardada só como hash)', 'Criar a conta, entrar e mandar os e-mails do serviço (códigos e avisos sobre as suas iniciativas)', 'Execução de contrato (art. 7º, V)'],
        ['O bairro que você escolhe (nome, cidade e estado)', 'Mostrar as iniciativas do seu bairro e publicar as suas nele', 'Execução de contrato (art. 7º, V)'],
        ['Iniciativas e atualizações que você publica, com o seu primeiro nome', 'Mostrar ao público o que o bairro precisa e o que já aconteceu', 'Execução de contrato (art. 7º, V)'],
        ['Seus apoios (quais iniciativas você apoiou)', 'Contar quantos vizinhos apoiam cada iniciativa e não contar o mesmo apoio duas vezes', 'Execução de contrato (art. 7º, V)'],
        ['Suas denúncias (qual iniciativa e o motivo)', 'Ocultar e revisar conteúdo que viola os Termos', 'Legítimo interesse (art. 7º, IX)'],
        ['Data e versão do aceite destes documentos', 'Comprovar o aceite', 'Exercício regular de direitos (art. 7º, VI)'],
        ['Registros de erro (sem nome, e-mail, IP nem conteúdo)', 'Corrigir falhas e lentidão', 'Legítimo interesse (art. 7º, IX)'],
        ['Endereço IP, data e hora', 'Limitar tentativas e bloquear abusos; o IP fica só na memória do servidor', 'Legítimo interesse (art. 7º, IX)'],
      ]} />
      <p>
        <strong>O que é público:</strong> as iniciativas e atualizações que você publica, com o seu <strong>primeiro
        nome</strong>, o bairro e as datas. Qualquer pessoa pode ler, mesmo sem conta. <strong>Nunca aparecem</strong> o seu
        sobrenome, o seu e-mail, quem apoiou cada iniciativa (só quantos) nem quem denunciou. Não escreva nas iniciativas
        dados seus ou de outras pessoas (endereço, telefone, documentos). Quando uma iniciativa é denunciada, a
        administração lê o texto, o primeiro nome do autor e os motivos das denúncias, para decidir se ela volta ou é
        removida. Não usamos localização, contatos do celular, fotos, anúncios nem ferramentas de rastreamento. No
        navegador, guardamos só o necessário para manter você conectado.
      </p>

      <h2>3. Com quem compartilhamos</h2>
      <Tabela cabecalho={['Fornecedor', 'Para quê', 'Onde']} linhas={[
        ['Render', 'Servidor da API', 'Estados Unidos'],
        ['Neon', 'Banco de dados', 'Estados Unidos (Virgínia), na mesma região do servidor'],
        ['Vercel', 'Hospedagem do site', 'Estados Unidos e rede global'],
        ['Resend', 'Envio dos e-mails (códigos e avisos sobre as suas iniciativas)', 'São Paulo, Brasil'],
        ['Sentry', 'Registro de erros, sem dados pessoais', 'Estados Unidos'],
        ['GitHub', 'Cópia de segurança diária do banco, criptografada', 'Estados Unidos'],
        ['ImprovMX e Google (Gmail)', 'Recebimento das mensagens enviadas ao nosso e-mail de contato', 'Estados Unidos'],
        ['Google Play', 'Distribuição do app; segue a política de privacidade do Google', 'Estados Unidos'],
      ]} />
      <p>
        Também podemos compartilhar dados quando exigido por lei ou ordem de autoridade competente. Não vendemos dados
        pessoais. Parte dos fornecedores fica fora do Brasil; essas transferências acontecem só para o serviço funcionar,
        com fornecedores que adotam medidas de segurança adequadas (art. 33 da LGPD).
      </p>

      <h2>4. Por quanto tempo guardamos</h2>
      <Tabela cabecalho={['Dado', 'Prazo']} linhas={[
        ['Conta, iniciativas, atualizações, apoios e denúncias', 'Enquanto a conta existir. Ao excluir a conta, tudo é apagado na hora'],
        ['Iniciativa apagada por você ou removida pela moderação', 'Apagada na hora, com os apoios e as atualizações'],
        ['Iniciativa oculta por denúncias e não revisada', '60 dias'],
        ['Denúncias', 'Até a administração decidir sobre a iniciativa'],
        ['Cadastros nunca confirmados', '30 dias'],
        ['Códigos enviados por e-mail e sessões', 'Até vencer, mais 1 dia'],
        ['Cópias de segurança criptografadas', '30 dias'],
        ['Registros de erro', 'Prazo do plano do Sentry, até 90 dias'],
      ]} />

      <h2 id="direitos">5. Seus direitos</h2>
      <p>
        Pela LGPD (art. 18), você pode pedir confirmação e acesso, correção, anonimização, bloqueio ou eliminação,
        portabilidade, informação sobre compartilhamento e revogação do consentimento. Em <strong>Conta</strong>, você
        corrige o nome, muda o bairro, baixa uma cópia completa dos seus dados (<strong>Baixar meus dados</strong>) e exclui a
        conta. Cada iniciativa sua pode ser editada ou apagada a qualquer momento. Se uma iniciativa de outra pessoa expõe
        você, use <strong>Denunciar</strong> ou escreva para <Email />. Respondemos em até 15 dias. Você também
        pode reclamar à Autoridade Nacional de Proteção de Dados (ANPD).
      </p>

      <h2>6. Segurança</h2>
      <p>
        HTTPS sempre; senhas com bcrypt; códigos por e-mail com validade curta e poucas tentativas; bloqueio após senhas erradas; limites de
        pedidos por IP; e cópia de segurança diária criptografada. Se acontecer um incidente que possa causar risco
        relevante, avisaremos você e a ANPD.
      </p>

      <h2>7. Idade mínima e mudanças</h2>
      <p>
        O {LEGAL.appName} é destinado a maiores de 18 anos. Quando esta Política mudar, atualizaremos a data no topo e
        avisaremos no site, no app ou por e-mail se a mudança for importante.
      </p>
    </Documento>
  )
}

export function TermosPage() {
  return (
    <Documento titulo="Termos de uso">
      <p>
        Estes Termos regulam o uso do {LEGAL.appName} (site {LEGAL.site}, aplicativo Android e API), mantido por{' '}
        {LEGAL.controller} ({LEGAL.controllerType}), {LEGAL.forum}. Ao criar uma conta, você declara que leu e aceita
        estes Termos e que está ciente da <Link to="/privacidade" className={estilos.link}>Política de privacidade</Link>.
      </p>

      <h2>1. O que é o Valtrea Pulso</h2>
      <p>
        Um espaço para moradores registrarem o que o bairro precisa (iluminação, limpeza, trânsito, áreas verdes e
        outros), reunirem o apoio dos vizinhos e contarem o que aconteceu depois. O serviço está em fase de testes e é
        gratuito.
      </p>
      <p>
        <strong>O Valtrea Pulso não é um canal oficial da prefeitura nem de nenhum órgão público</strong> e não tem ligação
        com partidos ou candidatos. Publicar uma iniciativa não abre pedido em nenhum órgão. Para isso, use os canais
        oficiais (como o 156 da sua cidade); o Pulso ajuda a organizar e mostrar quantos vizinhos querem o mesmo.
      </p>

      <h2>2. Conta</h2>
      <ul>
        <li>É preciso ter 18 anos ou mais e informar dados verdadeiros.</li>
        <li>Você é responsável pela sua senha e pelo que for feito na sua conta.</li>
        <li>Para publicar, apoiar e denunciar é preciso confirmar o e-mail. Cada pessoa tem uma só conta.</li>
        <li>O que você publica aparece para todos com o seu primeiro nome.</li>
      </ul>

      <h2>3. Uso permitido</h2>
      <p>Você concorda em não:</p>
      <ul>
        <li>publicar xingamentos, ameaças, preconceito ou ataques contra pessoas ou grupos;</li>
        <li>expor outras pessoas: nome, endereço, telefone, placa de carro, foto ou acusação contra alguém identificável;</li>
        <li>fazer propaganda, vender produtos ou serviços, ou pedir voto e fazer campanha política ou eleitoral;</li>
        <li>publicar informação falsa, golpes ou pedidos de dinheiro;</li>
        <li>criar contas falsas ou combinar apoios e denúncias em massa;</li>
        <li>tentar acessar dados que não são seus, explorar falhas ou sobrecarregar o serviço.</li>
      </ul>

      <h2>4. Denúncias e moderação</h2>
      <p>
        Qualquer pessoa com conta pode denunciar uma iniciativa, escolhendo o motivo. Quando três pessoas diferentes
        denunciam, a iniciativa fica oculta até a administração revisar. O autor recebe um e-mail e continua vendo a
        iniciativa. Na revisão, ela volta a aparecer ou é removida, e o autor é avisado. Podemos também remover conteúdo
        ou suspender contas que violem estes Termos, avisando o motivo pelo e-mail da conta, exceto quando houver risco a
        outras pessoas. Não concorda com uma decisão? Escreva para <Email />.
      </p>

      <h2>5. Disponibilidade e mudanças</h2>
      <p>
        Por ser uma versão de testes em hospedagem gratuita, o serviço pode demorar a responder ou ficar fora do ar, e
        funções podem mudar. Mudanças relevantes serão avisadas no site, no app ou por e-mail.
      </p>

      <h2>6. Responsabilidades</h2>
      <p>
        O conteúdo das iniciativas e atualizações é de responsabilidade de quem escreveu. Removemos conteúdo ilegal
        quando somos avisados, nos termos do Marco Civil da Internet (Lei nº 12.965/2014). Nada nestes Termos limita os direitos que
        você tem como consumidor pela legislação brasileira.
      </p>

      <h2>7. Encerramento, alterações e foro</h2>
      <p>
        Você pode excluir a conta a qualquer momento em <strong>Conta → Excluir conta</strong>. Se não concordar com uma
        nova versão destes Termos, pode excluir a conta sem custo. Estes Termos seguem as leis do Brasil; fica eleito o foro
        de {LEGAL.forum}, ressalvado o direito do consumidor de propor ação no foro do seu domicílio. Contato: <Email />.
      </p>
    </Documento>
  )
}

export function AcessibilidadePage() {
  return (
    <Documento titulo="Declaração de acessibilidade">
      <p>
        Queremos que o {LEGAL.appName} possa ser usado por todas as pessoas, incluindo pessoas com deficiência, como prevê a
        Lei Brasileira de Inclusão (Lei nº 13.146/2015, art. 63). Buscamos atender às WCAG 2.2, nível AA, e à ABNT NBR 17225.
      </p>
      <h2>O que já fizemos</h2>
      <ul>
        <li>As principais páginas são verificadas automaticamente (axe-core, WCAG A e AA) a cada mudança, no computador e no celular.</li>
        <li>Navegação completa pelo teclado, foco visível e link “Pular para o conteúdo”.</li>
        <li>Campos com rótulo visível e mensagens de erro em texto, ligadas ao campo.</li>
        <li>Contraste revisado no tema escuro; o zoom do navegador e do celular funciona em todas as páginas.</li>
        <li>No app Android, telas com os componentes do Material 3, que funcionam com o TalkBack.</li>
      </ul>
      <h2>Limitações conhecidas</h2>
      <ul>
        <li>Ainda não fizemos testes com pessoas que usam tecnologias assistivas, e não há tradução para Libras.</li>
      </ul>
      <h2>Encontrou uma barreira?</h2>
      <p>Escreva para <Email /> contando a página e o problema. Respondemos o quanto antes.</p>
    </Documento>
  )
}

export function ExcluirContaPage() {
  return (
    <Documento titulo="Como excluir a sua conta">
      <p>
        Entre no {LEGAL.appName} (site ou app) e vá em <strong>Conta → Excluir conta</strong>, confirmando com a sua senha.
        A conta, as iniciativas que você publicou (com os apoios e as atualizações delas), os seus apoios e as suas
        denúncias são apagados na hora.
      </p>
      <p>
        Sem acesso à conta? Use <Link to="/esqueci-senha" className={estilos.link}>Esqueci minha senha</Link> ou escreva
        para <Email /> com o assunto “Excluir conta”, a partir do e-mail cadastrado. Respondemos em até 15 dias.
      </p>
      <p>Cópias de segurança criptografadas são apagadas em até 30 dias, sem nenhum uso nesse período.</p>
    </Documento>
  )
}
